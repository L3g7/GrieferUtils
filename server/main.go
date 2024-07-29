package main

import (
	"errors"
	"fmt"
	"github.com/golang-jwt/jwt"
	"github.com/joho/godotenv"
	"log"
	"net/http"
	"os"
	"strings"
)

const VERSION = "1.1"

func main() {
	if err := godotenv.Load(); err != nil {
		log.Fatal("Could not load .env file")
	}

	// Initialize leaderboard
	go syncEmojis()

	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Strict-Transport-Security", "max-age=63072000; includeSubDomains; preload")
		w.Header().Set("Content-Type", "application/json")
		Error(w, http.StatusBadRequest, "Bad Request")
	})
	http.HandleFunc("/login", preprocess(true, LoginRoute))
	http.HandleFunc("/logout", preprocess(true, checkAuth(LogoutRoute)))
	http.HandleFunc("/keep_alive", preprocess(true, checkAuth(KeepAliveRoute)))

	http.HandleFunc("/hive_mind/block_of_the_day", preprocess(true, checkAuth(HiveMindBlockOfTheDayRoute)))
	http.HandleFunc("/hive_mind/mob_remover", preprocess(true, checkAuth(HiveMindMobRemoverRoute)))
	http.HandleFunc("/hive_mind/booster", preprocess(true, checkAuth(HiveMindBoosterRoute)))
	http.HandleFunc("/leaderboard", preprocess(false, checkAuth(LeaderboardRoute)))
	http.HandleFunc("/online_users", preprocess(true, checkAuth(OnlineUsersRoute)))
	http.HandleFunc("/admin", preprocess(true, AdminRoute))

	err := http.ListenAndServe(":3333", nil)
	if errors.Is(err, http.ErrServerClosed) {
		fmt.Printf("server closed\n")
	} else if err != nil {
		fmt.Printf("error starting server: %s\n", err)
		os.Exit(1)
	}
}

// preprocess adds a handler middleware applying common validity checks and headers.
func preprocess(checkMethod bool, handler func(http.ResponseWriter, *http.Request) error) func(http.ResponseWriter, *http.Request) {
	return func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Strict-Transport-Security", "max-age=63072000; includeSubDomains; preload")
		w.Header().Set("Content-Type", "application/json")

		if r.Method != "POST" && checkMethod {
			Error(w, http.StatusMethodNotAllowed, "Method Not Allowed")
			return
		}

		if r.Header.Get("Content-Type") != "application/json" && checkMethod {
			Error(w, http.StatusUnsupportedMediaType, "Unsupported Media Type")
			return
		}

		err := handler(w, r)
		if err != nil {
			Error(w, http.StatusBadRequest, "Bad Request")
			ReportBug(r, err)
		}
	}
}

// checkAuth adds a handler middleware checking whether the request is authorized.
func checkAuth(handler func(http.ResponseWriter, *http.Request, *jwt.Token) error) func(http.ResponseWriter, *http.Request) error {
	return func(w http.ResponseWriter, r *http.Request) error {

		// Extract session token
		jwtToken := r.Header.Get("Authorization")
		if !strings.HasPrefix(jwtToken, "Bearer ") {
			Error(w, http.StatusUnauthorized, "Unauthorized")
			return nil
		}

		// Parse session token
		token, err := jwt.Parse(jwtToken[7:], func(token *jwt.Token) (interface{}, error) {
			_, ok := token.Method.(*jwt.SigningMethodHMAC)
			if !ok {
				return "", nil
			}
			return []byte(os.Getenv("JWT_SECRET")), nil
		})

		if err != nil {
			Error(w, http.StatusUnauthorized, "Unauthorized")
			return nil
		}

		// Check if user is banned
		claims, _ := token.Claims.(jwt.MapClaims)
		user := claims["sub"].(string)
		if IsBanned(user) {
			Error(w, http.StatusUnauthorized, "Unauthorized")
			return nil
		}

		return handler(w, r, token)
	}
}

func Error(w http.ResponseWriter, code int, message string) {
	w.WriteHeader(code)
	_, _ = fmt.Fprintf(w, `{"status":%d,"message":"%s"}`, code, message)
}
