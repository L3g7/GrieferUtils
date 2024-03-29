package main

import (
	"fmt"
	"github.com/golang-jwt/jwt"
	"net/http"
)

func LogoutRoute(w http.ResponseWriter, r *http.Request, token *jwt.Token) error {
	// Parse request
	var request OnlineUsersRequest
	err := DecodeFully(r.Body, &request)
	if err != nil {
		return err
	}

	claims, _ := token.Claims.(jwt.MapClaims)
	OnlineUsersMutex.Lock()
	delete(OnlineUsers, claims["sub"].(string))
	OnlineUsersMutex.Unlock()

	// Send response
	w.WriteHeader(http.StatusNoContent)
	_, _ = fmt.Fprintf(w, `\n`)
	return nil
}
