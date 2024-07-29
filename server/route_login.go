package main

import (
	"crypto"
	"crypto/rsa"
	"crypto/sha1"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"encoding/binary"
	"encoding/hex"
	"fmt"
	"github.com/golang-jwt/jwt"
	"net/http"
	"os"
	"regexp"
	"strings"
	"time"
)

var UuidRegex, _ = regexp.Compile("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")

type LoginRequest struct {
	User           string `json:"user"`
	RequestTime    int64  `json:"request_time"`
	Signature      string `json:"signature"`
	PublicKey      string `json:"public_key"`
	KeySignature   string `json:"key_signature"`
	ExpirationTime uint64 `json:"expiration_time"`
}

func LoginRoute(w http.ResponseWriter, r *http.Request) error {
	// Parse request
	var request LoginRequest
	err := Decode(r.Body, &request)
	if err != nil {
		return nil
	}

	// Check if user is valid
	if !UuidRegex.MatchString(request.User) {
		Error(w, http.StatusBadRequest, "Bad Request")
		return nil
	}

	// Check if RequestTime is reasonable
	if request.RequestTime < 0 || time.UnixMilli(request.RequestTime).Add(30*time.Second).Before(time.Now()) {
		// RequestTime is more than 30s ago
		Error(w, http.StatusBadRequest, "Bad Request")
		return nil
	}

	// Extract user
	rawUuid, err := hex.DecodeString(strings.ReplaceAll(request.User, "-", ""))
	if err != nil {
		return nil
	}

	// Extract public key
	rawKey, err := base64.StdEncoding.DecodeString(request.PublicKey)
	if err != nil {
		return nil
	}

	// Parse public key
	key, err := x509.ParsePKIXPublicKey(rawKey)
	if err != nil {
		return nil
	}
	rsaKey := key.(*rsa.PublicKey)

	// Extract signature
	signature, err := base64.StdEncoding.DecodeString(request.Signature)
	if err != nil {
		return nil
	}

	// Create signed payload
	payload := binary.BigEndian.AppendUint64(rawUuid, uint64(request.RequestTime))
	hash := sha256.Sum256(payload)

	// Validate signed payload
	err = rsa.VerifyPKCS1v15(rsaKey, crypto.SHA256, hash[:], signature)
	if err != nil {
		return nil
	}

	// Extract key signature
	signature, err = base64.StdEncoding.DecodeString(request.KeySignature)
	if err != nil {
		return nil
	}

	// Create payload signed by key signature
	payload = binary.BigEndian.AppendUint64(rawUuid, request.ExpirationTime)
	payload = append(payload, rawKey...)
	keyHash := sha1.Sum(payload)

	// Verify public key
	for _, pubKey := range YggdrasilSessionPubKeys {
		err = rsa.VerifyPKCS1v15(pubKey, crypto.SHA1, keyHash[:], signature)
		if err == nil {
			break
		}
	}
	if err != nil {
		return nil
	}

	// Authentication complete, create session token
	OnlineUsersMutex.Lock()
	OnlineUsers[request.User] = time.Now().Unix()
	OnlineUsersMutex.Unlock()
	token := jwt.NewWithClaims(jwt.SigningMethodHS384, &jwt.MapClaims{
		"exp": time.Now().Add(24 * time.Hour).Unix(),
		"iat": time.Now().Unix(),
		"iss": "https://s1.grieferutils.l3g7.dev/login",
		"sub": request.User,
	})

	tokenString, err := token.SignedString([]byte(os.Getenv("JWT_SECRET")))
	if err != nil {
		fmt.Printf("Error while signing token: %s\n", err)
		Error(w, http.StatusInternalServerError, "Internal Server Error")
		return nil
	}

	_, _ = fmt.Fprintf(w, `{"session_token":"%s"}`, tokenString)
	return nil
}
