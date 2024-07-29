package main

import (
	"fmt"
	"github.com/golang-jwt/jwt"
	"net/http"
)

type LogoutRequest struct{}

func LogoutRoute(w http.ResponseWriter, r *http.Request, token *jwt.Token) error {
	// Parse request
	var request LogoutRequest
	err := Decode(r.Body, &request)
	if err != nil {
		return nil
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
