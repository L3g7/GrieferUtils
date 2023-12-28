package main

import (
	"encoding/json"
	"fmt"
	"github.com/golang-jwt/jwt"
	"net/http"
	"os"
	"time"
)

type AdminRequest struct {
	Action string  `json:"action"`
	Data   *string `json:"data,omitempty"`
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
			"sub": *request.Data,
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

	Error(w, http.StatusBadRequest, "Bad Request")
	return nil
}
