package main

import (
	"encoding/binary"
	"encoding/json"
	"fmt"
	"github.com/boltdb/bolt"
	"github.com/golang-jwt/jwt"
	"net/http"
	"os"
	"time"
)

type AdminRequest struct {
	Action string  `json:"action"`
	User   *string `json:"user,omitempty"`
	Score  *uint32 `json:"score,omitempty"`
}

// AdminRoute is private, so there's not much request feedback.
func AdminRoute(w http.ResponseWriter, r *http.Request) error {
	if r.Header.Get("Authorization") != os.Getenv("ADMIN_TOKEN") {
		Error(w, http.StatusUnauthorized, "Unauthorized")
		return nil
	}

	var request AdminRequest
	err := DecodeFully(r.Body, &request)
	if err != nil {
		return err
	}

	if request.Action == "updateLeaderboard" {
		go updateLeaderboard()
		w.WriteHeader(http.StatusNoContent)
		_, _ = fmt.Fprintf(w, `\n`)
		return nil
	}

	if request.Action == "genToken" {
		tokenString, _ := jwt.NewWithClaims(jwt.SigningMethodHS384, &jwt.MapClaims{
			"exp": time.Now().Add(24 * time.Hour).Unix(),
			"iat": time.Now().Unix(),
			"iss": "https://s1.grieferutils.l3g7.dev/login",
			"sub": *request.User,
		}).SignedString([]byte(os.Getenv("JWT_SECRET")))
		_, _ = w.Write([]byte(tokenString))
		return nil
	}

	if request.Action == "dumpUsers" {
		OnlineUsersMutex.Lock()
		data, _ := json.Marshal(OnlineUsers)
		OnlineUsersMutex.Unlock()
		_, _ = w.Write(data)
		return nil
	}

	if request.Action == "setLeaderboardEntry" {
		user := *request.User
		return db.Update(func(tx *bolt.Tx) error {
			b := tx.Bucket([]byte("leaderboard"))
			scores := b.Bucket([]byte("scores"))

			if request.Score == nil {
				return scores.Delete([]byte(user))
			} else {
				return scores.Put([]byte(user), binary.LittleEndian.AppendUint32([]byte{}, *request.Score))
			}
		})
	}

	Error(w, http.StatusBadRequest, "Bad Request")
	return nil
}
