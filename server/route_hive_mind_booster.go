package main

import (
	"encoding/json"
	"fmt"
	"github.com/golang-jwt/jwt"
	"net/http"
	"os"
	"slices"
	"strings"
	"sync"
	"time"
)

type HiveMindBoosterRequest struct {
	Citybuild       string           `json:"citybuild"`
	Value           *HiveMindBooster `json:"value,omitempty"`
	MaxAvgDevToPrev *uint64          `json:"max_avg_dev_to_prev,omitempty"`
}

type HiveMindBooster struct {
	Break []uint64 `json:"break,omitempty"`
	Drop  []uint64 `json:"drop,omitempty"`
	Fly   []uint64 `json:"fly,omitempty"`
	Mob   []uint64 `json:"mob,omitempty"`
	XP    []uint64 `json:"xp,omitempty"`
}

type HiveMindBoosterEntry struct {
	User      string `json:"user"`
	Timestamp uint64 `json:"timestamp"`
	Value     []uint64 `json:"value"`
}

type HiveMindBoosterResponse struct {
	Known bool `json:"known"`
	HiveMindBooster
}

// Break, Drop, Fly, Mob, XP
// boosterKnowledge stores all data collected by the hive mind in a map of Citybuild -> Type -> []Entry
var boosterKnowledge = make(map[string]map[string][]HiveMindBoosterEntry)
var boosterKnowledgeMutex sync.Mutex

func HiveMindBoosterRoute(w http.ResponseWriter, r *http.Request, token *jwt.Token) error {
	validCitybuilds := strings.Split(os.Getenv("VALID_CITYBUILDS"), ",")

	claims, _ := token.Claims.(jwt.MapClaims)
	user := claims["sub"].(string)

	// Parse request
	var request HiveMindBoosterRequest
	err := Decode(r.Body, &request)
	if err != nil {
		return nil
	}

	citybuild := request.Citybuild
	if !slices.Contains(validCitybuilds, citybuild) {
		Error(w, http.StatusBadRequest, "Bad Request")
		return nil
	}

	var response *HiveMindBoosterResponse

	boosterKnowledgeMutex.Lock()
	defer boosterKnowledgeMutex.Unlock()

	entries := boosterKnowledge[citybuild]
	if entries == nil {
		entries = make(map[string][]HiveMindBoosterEntry)
		boosterKnowledge[citybuild] = entries
	}
	now := uint64(time.Now().Unix())

	if request.Value != nil {
		value := request.Value

		// Remove old entries
		foreach(*value, func(_ []uint64, key string) {
			entries[key] = Remove(entries[key], func(entry HiveMindBoosterEntry) bool {
				return entry.User == user
			})
		})

		// Validate new entries
		valid := true
		foreach(*value, func(v []uint64, _ string) {
			for _, ts := range v {
				if ts < now || ts > now+5400000 {
					// entry has passed or is more than 90 min away
					valid = false
				}
			}
			if len(v) > 5 {
				// more than 5 boosters are simultaneously active
				valid = false
			}
		})
		if !valid {
			// New entry is more than 20 min away
			Error(w, http.StatusBadRequest, "Bad Request")
			return nil
		}

		// Add new entries
		foreach(*value, func(v []uint64, key string) {
			entries[key] = append(entries[key], HiveMindBoosterEntry{user, now, v})
		})
	} else if request.MaxAvgDevToPrev != nil {
		response = &HiveMindBoosterResponse{}
		// Remove entries that already passed
		foreach(response.HiveMindBooster, func(_ []uint64, key string) {
			for _, v := range entries[key] {
				v.Value = Remove(v.Value, func(ts uint64) bool {
					return now > ts
				})
			}
		})

		// Collect values
		empty := true
		collect := func(key string) []uint64 {
			value := HiveMindBoosterEntry{}

			for i := 0; i <= 5; i++ {
				entries := Remove(entries[key], func(entry HiveMindBoosterEntry) bool {
					return len(entry.Value) == i
				})

				considered := GetWithLeastDeviation(entries, func(first HiveMindBoosterEntry, second HiveMindBoosterEntry) *uint64 {
					var deviation uint64
					for i := 0; i < min(len(first.Value), len(second.Value)); i++ {
						deviation += diff(first.Value[i], second.Value[i])
					}
					return &deviation
				}, func(val HiveMindBoosterEntry) HiveMindBoosterEntry {
					return val
				})

				if considered == nil {
					continue
				}

				empty = false

				if len(value.Value) == 0 {
					value = *considered
				} else if len(considered.Value) > len(value.Value) {
					// check whether considered value is newer
					if considered.Timestamp > value.Timestamp {

						// Check whether deviation to previous value is in acceptable range
						avgDeviation := new(Avg)
						for i := range value.Value {
							avgDeviation.add(diff(value.Value[i], considered.Value[i]))
						}

						if avgDeviation.get() < *request.MaxAvgDevToPrev {
							value = *considered
						}
					}
				}
			}
			return value.Value
		}

		response.Break = collect("break")
		response.Drop = collect("drop")
		response.Fly = collect("fly")
		response.Mob = collect("mob")
		response.XP = collect("xp")

		response.Known = !empty
	} else {
		// Remove old entries
		foreach(HiveMindBooster{}, func(_ []uint64, key string) {
			entries[key] = Remove(entries[key], func(entry HiveMindBoosterEntry) bool {
				return entry.User == user
			})
		})
	}

	if response != nil {
		_ = json.NewEncoder(w).Encode(*response)
	} else {
		_, _ = fmt.Fprint(w, "{}")
	}

	return nil
}

func foreach(booster HiveMindBooster, fn func(v []uint64, key string)) {
	fn(booster.Break, "break")
	fn(booster.Drop, "drop")
	fn(booster.Fly, "fly")
	fn(booster.Mob, "mob")
	fn(booster.XP, "xp")
}
