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

type HiveMindMobRemoverRequest struct {
	Citybuild string  `json:"citybuild"`
	Value     *uint64 `json:"value,omitempty"`
}

type HiveMindMobRemoverResponse struct {
	Value *uint64 `json:"value,omitempty"`
}

type HiveMindMobRemoverEntry struct {
	User  string
	Value uint64
}

// mobRemoverKnowledge stores all data collected by the hive mind in a map of Citybuild -> []Entry
var mobRemoverKnowledge = make(map[string][]HiveMindMobRemoverEntry)
var mobRemoverKnowledgeMutex sync.Mutex

func HiveMindMobRemoverRoute(w http.ResponseWriter, r *http.Request, token *jwt.Token) error {
	validCitybuilds := strings.Split(os.Getenv("VALID_CITYBUILDS"), ",")

	claims, _ := token.Claims.(jwt.MapClaims)
	user := claims["sub"].(string)

	// Parse request
	var request HiveMindMobRemoverRequest
	err := DecodeFully(r.Body, &request)
	if err != nil {
		return err
	}

	citybuild := request.Citybuild
	if !slices.Contains(validCitybuilds, citybuild) {
		Error(w, http.StatusBadRequest, "Bad Request")
		return nil
	}

	var response HiveMindMobRemoverResponse

	mobRemoverKnowledgeMutex.Lock()
	defer mobRemoverKnowledgeMutex.Unlock()

	entries := mobRemoverKnowledge[citybuild]

	now := uint64(time.Now().Unix())

	if request.Value != nil {
		// Remove old entry
		entries := Remove(entries, func(entry HiveMindMobRemoverEntry) bool {
			return entry.User == user
		})

		// Validate new entry
		if *request.Value > now+1200000 {
			// New entry is more than 20 min away
			Error(w, http.StatusBadRequest, "Bad Request")
			return nil
		}

		// Add new entry
		entries = append(entries, HiveMindMobRemoverEntry{
			User:  user,
			Value: *request.Value,
		})
		mobRemoverKnowledge[citybuild] = entries
	} else {
		// Remove entries that already passed
		entries = Remove(entries, func(entry HiveMindMobRemoverEntry) bool {
			return now > entry.Value
		})
		mobRemoverKnowledge[citybuild] = entries

		// Get value with least avg deviation to other values
		response.Value = GetWithLeastDeviation(entries, func(first HiveMindMobRemoverEntry, second HiveMindMobRemoverEntry) *uint64 {
			if first != second {
				deviation := diff(first.Value, second.Value)
				return &deviation
			}
			return nil
		}, func(val HiveMindMobRemoverEntry) uint64 {
			return val.Value
		})
	}

	_ = json.NewEncoder(w).Encode(response)
	return nil
}
