package main

import (
	"fmt"
	"github.com/golang-jwt/jwt"
	"net/http"
	"time"
)

func KeepAliveRoute(w http.ResponseWriter, _ *http.Request, token *jwt.Token) error {
	claims, _ := token.Claims.(jwt.MapClaims)
	OnlineUsersMutex.Lock()
	OnlineUsers[claims["sub"].(string)] = time.Now().Unix()
	OnlineUsersMutex.Unlock()

	// Send response
	w.WriteHeader(http.StatusNoContent)
	_, _ = fmt.Fprintf(w, `\n`)
	return nil
}
