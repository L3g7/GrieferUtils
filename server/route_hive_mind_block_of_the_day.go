package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"github.com/golang-jwt/jwt"
	"math"
	"net/http"
	"os"
	"slices"
	"sync"
	"time"
)

type HiveMindBOTDRequest struct {
	// Item represents a Minecraft item, encoded using NBT to JSON serialization
	Item      string `json:"item"`
	Timestamp uint64 `json:"timestamp"`
}

// BOTDReportedBlocks stores all data in a map of Item -> []User reporting the item
var BOTDReportedBlocks = make(map[string][]string)

// BOTDReverseMap stores all data in a map of User -> Item
var BOTDReverseMap = make(map[string]string)
var BOTDFaulty = false

var BOTDLastPublishedTimestamp uint64 = 0
var BOTDCurrentDay uint64 = 0
var BOTDMutex sync.Mutex

func HiveMindBlockOfTheDayRoute(w http.ResponseWriter, r *http.Request, token *jwt.Token) error {
	claims, _ := token.Claims.(jwt.MapClaims)
	user := claims["sub"].(string)

	// Parse HiveMindBOTDRequest
	var request HiveMindBOTDRequest
	err := Decode(r.Body, &request)
	if err != nil {
		return nil
	}

	// Send response
	w.WriteHeader(http.StatusNoContent)
	_, _ = fmt.Fprintf(w, `\n`)

	now := uint64(time.Now().Unix())
	currentDay := now/86400*86400 + 7200 // Updates happen at 02:00 GMT+0 every night
	if currentDay > now {
		currentDay -= 86400
	}

	// Check if HiveMindBOTDRequest is up-to-date
	if request.Timestamp > uint64(time.Now().Unix()) || request.Timestamp < currentDay {
		return nil
	}

	BOTDMutex.Lock()
	defer BOTDMutex.Unlock()

	if BOTDCurrentDay != currentDay {
		// New day started, reset data
		BOTDReportedBlocks = make(map[string][]string)
		BOTDReverseMap = make(map[string]string)
		BOTDFaulty = false
		BOTDCurrentDay = currentDay
	}

	// Only allow one reported block per day
	if _, ok := BOTDReverseMap[user]; ok {
		return nil
	}

	// Store reported block
	BOTDReverseMap[user] = request.Item
	users, ok := BOTDReportedBlocks[request.Item]
	if !ok {
		newUsers := [1]string{user}
		users = newUsers[:]
		BOTDReportedBlocks[request.Item] = users
		return nil
	}

	// Check whether multiple blocks were reported
	if !BOTDFaulty && len(BOTDReportedBlocks) > 1 {
		BOTDFaulty = true
		ReportBug("BOTDFaulty block of the day data")
		return nil
	}

	// Store in BOTDReportedBlocks
	if !slices.Contains(users, user) {
		users = append(users, user)
		BOTDReportedBlocks[request.Item] = users

		// Block was reported by at least one other user, send to Discord channel
		if BOTDLastPublishedTimestamp >= currentDay {
			return nil
		}

		BOTDLastPublishedTimestamp = math.MaxUint64 // Don't call publicBlockOfTheDay twice
		go publishBlockOfTheDay(request.Item, currentDay)
	}

	return nil
}

func publishBlockOfTheDay(item string, day uint64) {
	data, err := json.Marshal(HiveMindBOTDRequest{item, day})
	if err != nil {
		ReportBug("Could not marshal block of the day", err)
		BOTDLastPublishedTimestamp = 0
		return
	}

	req, err := http.NewRequest("POST", os.Getenv("API_ROUTE")+"/publish_block_of_the_day", bytes.NewBuffer(data))
	if err != nil {
		ReportBug("Could create block of the day HiveMindBOTDRequest", err)
		BOTDLastPublishedTimestamp = 0
		return
	}

	req.Header.Set("Authorization", os.Getenv("ADMIN_TOKEN"))
	req.Header.Set("Content-Type", "application/json")

	_, err = http.DefaultClient.Do(req)
	if err != nil {
		ReportBug("Could publish block of the day", err)
		BOTDLastPublishedTimestamp = 0
	} else {
		BOTDLastPublishedTimestamp = day
	}
}
