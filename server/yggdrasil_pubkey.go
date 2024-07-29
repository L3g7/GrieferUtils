package main

import (
	"crypto/rsa"
	"crypto/x509"
	"encoding/base64"
	"log"
	"net/http"
	"sync"
)

var YggdrasilSessionPubKeys = CreateYggdrasilPubKeys()
var YggdrasilSessionPubKeyMutex sync.RWMutex

func loadYggdrasilPubKeys() []*rsa.PublicKey {
	// Request keys
	res, err := http.Get("https://api.minecraftservices.com/publickeys")
	if err != nil {
		return nil
	}

	certs := struct {
		CertKeys []struct {
			PubKey string `json:"publicKey"`
		} `json:"playerCertificateKeys"`
	}{}
	err = DecodeLossy(res.Body, &certs)
	if err != nil {
		return nil
	}

	// Decode keys
	var keys = make([]*rsa.PublicKey, len(certs.CertKeys))
	for index, key := range certs.CertKeys {
		bytes, err := base64.StdEncoding.DecodeString(key.PubKey)
		if err != nil {
			return nil
		}

		key, err := x509.ParsePKIXPublicKey(bytes)
		if err != nil {
			return nil
		}

		keys[index] = key.(*rsa.PublicKey)
	}

	return keys
}

func CreateYggdrasilPubKeys() []*rsa.PublicKey {
	pubKeys := loadYggdrasilPubKeys()
	if pubKeys == nil {
		log.Fatal("Could not load yggdrasil keys")
	}

	return pubKeys
}
