package main

import (
	"encoding/json"
	"errors"
	"fmt"
	"github.com/golang-jwt/jwt"
	"github.com/joho/godotenv"
	"io"
	"log"
	"net/http"
	"os"
	"strings"
)

func main() {
	if err := godotenv.Load(); err != nil {
		log.Fatal("Could not load .env file")
	}

	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Strict-Transport-Security", "max-age=63072000; includeSubDomains; preload")
		w.Header().Set("Content-Type", "application/json")
		Error(w, http.StatusBadRequest, "Bad Request")
	})
	http.HandleFunc("/login", preprocess(LoginRoute))
	http.HandleFunc("/online_users", preprocess(checkAuth(OnlineUsersRoute)))
	http.HandleFunc("/logout", preprocess(checkAuth(LogoutRoute)))
	http.HandleFunc("/keep_alive", preprocess(checkAuth(KeepAliveRoute)))
	http.HandleFunc("/hive_mind", preprocess(checkAuth(HiveMindRoute)))

	err := http.ListenAndServe(":3333", nil)
	if errors.Is(err, http.ErrServerClosed) {
		fmt.Printf("server closed\n")
	} else if err != nil {
		fmt.Printf("error starting server: %s\n", err)
		os.Exit(1)
	}
}

// preprocess adds a handler middleware applying common validity checks and headers.
func preprocess(handler func(http.ResponseWriter, *http.Request) error) func(http.ResponseWriter, *http.Request) {
	return func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Strict-Transport-Security", "max-age=63072000; includeSubDomains; preload")
		w.Header().Set("Content-Type", "application/json")

		if r.Method != "POST" {
			Error(w, http.StatusMethodNotAllowed, "Method Not Allowed")
			return
		}

		if r.Header.Get("Content-Type") != "application/json" {
			Error(w, http.StatusUnsupportedMediaType, "Unsupported Media Type")
			return
		}

		err := handler(w, r)
		if err != nil {
			Error(w, http.StatusBadRequest, "Bad Request")
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

		return handler(w, r, token)
	}
}

func DecodeFully(r io.Reader, v any) error {
	dec := json.NewDecoder(r)
	dec.DisallowUnknownFields()

	err := dec.Decode(v)
	if err != nil {
		return err
	}

	err = dec.Decode(&struct{}{})
	if err != io.EOF {
		return errors.New("JSON not fully decoded")
	}

	return nil
}

func Error(w http.ResponseWriter, code int, message string) {
	w.WriteHeader(code)
	fmt.Fprintf(w, `{"status":%d,"message":"%s"}`, code, message)
}
