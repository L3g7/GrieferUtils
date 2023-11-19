package main

import (
	"encoding/json"
	"github.com/golang-jwt/jwt"
	"net/http"
	"os"
	"slices"
	"strings"
	"sync"
	"time"
)

type HiveMindRequest struct {
	CityBuild string                          `json:"city_build"`
	Data      map[string]HiveMindRequestEntry `json:"data"`
}

type HiveMindRequestEntry struct {
	Value  *uint64 `json:"value,omitempty"`
	MaxAge *uint64 `json:"max_age,omitempty"`
}

type HiveMindEntry struct {
	Value     uint64
	Timestamp uint64
}

// entries stores all data collected by the hive mind in a map of Name -> CityBuild -> User -> Entry
var entries = make(map[string]map[string]map[string]HiveMindEntry)
var entriesMutex sync.Mutex

var validNames = strings.Split(os.Getenv("HIVE_MIND_VALID_NAMES"), ",")
var validCityBuilds = strings.Split(os.Getenv("HIVE_MIND_VALID_CITY_BUILDS"), ",")

func HiveMindRoute(w http.ResponseWriter, r *http.Request, token *jwt.Token) error {
	claims, _ := token.Claims.(jwt.MapClaims)
	user := claims["sub"].(string)

	// Parse request
	var request HiveMindRequest
	err := DecodeFully(r.Body, &request)
	if err != nil {
		return err
	}

	cityBuild := request.CityBuild
	if !slices.Contains(validCityBuilds, cityBuild) {
		Error(w, http.StatusBadRequest, "Bad Request")
		return nil
	}

	response := make(map[string]uint64)

	entriesMutex.Lock()

	for name, element := range request.Data {
		if !slices.Contains(validNames, name) {
			// Just ignore invalid entries, no error necessary
			continue
		}

		cityBuilds, existing := entries[name]
		if !existing {
			cityBuilds = make(map[string]map[string]HiveMindEntry)
			entries[name] = cityBuilds
		}

		users, existing := cityBuilds[cityBuild]
		if !existing {
			users = make(map[string]HiveMindEntry)
			cityBuilds[cityBuild] = users
		}

		if element.Value != nil {
			// Add value to collective knowledge
			users[user] = HiveMindEntry{
				*element.Value,
				uint64(time.Now().Unix()),
			}
		} else if element.MaxAge != nil {
			now := uint64(time.Now().Unix())
			maxAge := *element.MaxAge

			// Remove entry if more than 1h old
			for k, v := range users {
				if now-v.Timestamp > 3600 {
					delete(users, k)
				}
			}

			// Get value with least avg deviation to other values

			var consideredValue *uint64 = nil
			var consideredAvgDeviation uint64 = 0
			consideredAvgDeviation-- // create underflow

			for k, v := range users {
				if v.Timestamp < now-maxAge {
					continue
				}

				var deviationSum uint64 = 0
				var deviationCount uint64 = 0
				for k2, v := range users {
					if v.Timestamp > now-maxAge && k != k2 {
						deviationSum += v.Value
						deviationCount++
					}
				}
				avgDeviation := deviationSum / deviationCount

				if avgDeviation < consideredAvgDeviation {
					consideredValue = &v.Value
					consideredAvgDeviation = avgDeviation
				}
			}

			if consideredValue != nil {
				response[name] = *consideredValue
			}
		}
	}

	entriesMutex.Unlock()

	_ = json.NewEncoder(w).Encode(response)
	return nil
}
