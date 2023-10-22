package main

import (
	"encoding/json"
	"github.com/golang-jwt/jwt"
	"net/http"
	"time"
)

// OnlineUsers stores users and the last time they sent a keep_alive request.
var OnlineUsers = make(map[string]int64)

type OnlineUsersRequest struct {
	UsersRequested []string `json:"users_requested"`
}
type OnlineUsersResponse struct {
	UsersOnline []string `json:"users_online"`
}

func OnlineUsersRoute(w http.ResponseWriter, r *http.Request, _ *jwt.Token) error {
	// Parse request
	var request OnlineUsersRequest
	err := DecodeFully(r.Body, &request)
	if err != nil {
		return err
	}

	// Check whether the users requested are online
	usersOnline := make([]string, 0)
	for _, user := range request.UsersRequested {
		ts, existing := OnlineUsers[user]
		if !existing {
			continue
		}

		if time.Unix(ts, 0).Add(30 * time.Second).Before(time.Now()) {
			delete(OnlineUsers, user)
		} else {
			usersOnline = append(usersOnline, user)
		}
	}

	// Send response
	_ = json.NewEncoder(w).Encode(OnlineUsersResponse{usersOnline})
	return nil
}
