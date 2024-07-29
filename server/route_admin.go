package main

import (
	"encoding/binary"
	"encoding/json"
	"fmt"
	"github.com/golang-jwt/jwt"
	bolt "go.etcd.io/bbolt"
	"net/http"
	"os"
	"time"
)

type AdminRequest struct {
	Action string  `json:"action"`
	Data   *string `json:"data,omitempty"`
	Score  *uint32 `json:"score,omitempty"`
}

// AdminRoute is private, so there's not much request feedback.
func AdminRoute(w http.ResponseWriter, r *http.Request) error {
	if r.Header.Get("Authorization") != os.Getenv("ADMIN_TOKEN") {
		Error(w, http.StatusUnauthorized, "Unauthorized")
		return nil
	}

	var request AdminRequest
	err := Decode(r.Body, &request)
	if err != nil {
		return err
	}

	if request.Action == "genToken" {
		tokenString, _ := jwt.NewWithClaims(jwt.SigningMethodHS384, &jwt.MapClaims{
			"exp": time.Now().Add(24 * time.Hour).Unix(),
			"iat": time.Now().Unix(),
			"iss": "https://s1.grieferutils.l3g7.dev/login",
			"sub": *request.Data,
		}).SignedString([]byte(os.Getenv("JWT_SECRET")))
		_, _ = w.Write([]byte(tokenString))
		return nil
	}

	if request.Action == "updateLeaderboard" {
		go updateLeaderboard()
		w.WriteHeader(http.StatusNoContent)
		_, _ = fmt.Fprintf(w, `\n`)
		return nil
	}

	if request.Action == "setLeaderboardEntry" {
		user := *request.Data
		_ = db.Update(func(tx *bolt.Tx) error {
			b := tx.Bucket([]byte("leaderboard"))
			scores := b.Bucket([]byte("scores"))

			if request.Score == nil {
				return scores.Delete([]byte(user))
			} else {
				return scores.Put([]byte(user), binary.LittleEndian.AppendUint32([]byte{}, *request.Score))
			}
		})
		w.WriteHeader(http.StatusNoContent)
		_, _ = fmt.Fprintf(w, `\n`)
		return nil
	}

	if request.Action == "dumpUsers" {
		OnlineUsersMutex.Lock()
		data, _ := json.Marshal(OnlineUsers)
		OnlineUsersMutex.Unlock()
		_, _ = w.Write(data)
		return nil
	}

	if request.Action == "updateYggdrasilKeys" {
		data := loadYggdrasilPubKeys()
		if data == nil {
			w.WriteHeader(http.StatusInternalServerError)
			_, _ = fmt.Fprintf(w, `\n`)
			return nil
		}

		YggdrasilSessionPubKeyMutex.Lock()
		YggdrasilSessionPubKeys = data
		YggdrasilSessionPubKeyMutex.Unlock()

		w.WriteHeader(http.StatusNoContent)
		_, _ = fmt.Fprintf(w, `\n`)
		return nil
	}

	if request.Action == "getBooster" {
		boosterKnowledgeMutex.Lock()
		data, _ := json.Marshal(boosterKnowledge)
		boosterKnowledgeMutex.Unlock()
		_, _ = w.Write(data)
		return nil
	}

	if request.Action == "setBooster" {
		var data map[string]map[string][]HiveMindBoosterEntry
		err := json.Unmarshal([]byte(*request.Data), &data)
		if err != nil {
			return err
		}

		boosterKnowledgeMutex.Lock()
		boosterKnowledge = data
		boosterKnowledgeMutex.Unlock()

		w.WriteHeader(http.StatusNoContent)
		_, _ = fmt.Fprintf(w, `\n`)
		return nil
	}

	if request.Action == "getMobRemover" {
		mobRemoverKnowledgeMutex.Lock()
		data, _ := json.Marshal(mobRemoverKnowledge)
		mobRemoverKnowledgeMutex.Unlock()
		_, _ = w.Write(data)
		return nil
	}

	if request.Action == "setMobRemover" {
		var data map[string][]HiveMindMobRemoverEntry
		err := json.Unmarshal([]byte(*request.Data), &data)
		if err != nil {
			return err
		}

		mobRemoverKnowledgeMutex.Lock()
		mobRemoverKnowledge = data
		mobRemoverKnowledgeMutex.Unlock()

		w.WriteHeader(http.StatusNoContent)
		_, _ = fmt.Fprintf(w, `\n`)
		return nil
	}

	Error(w, http.StatusBadRequest, "Unknown action")
	return nil
}
