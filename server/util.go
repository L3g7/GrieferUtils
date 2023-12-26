package main

import (
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"github.com/boltdb/bolt"
	"io"
	"log"
	"math"
	"net/http"
)

func Decode(r io.Reader, v any) error {
	return json.NewDecoder(r).Decode(v)
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

// Remove removes any entries matching the filter from an unsorted array.
func Remove[T any](array []T, filter func(T) bool) []T {
	i := 0
	for i < len(array) {
		v := array[i]
		if filter(v) {
			array[i] = array[len(array)-1]
			array = array[:len(array)-1]
		} else {
			i++
		}
	}

	return array
}

// GetWithLeastDeviation returns the entry with the least average deviation from all other entries.
func GetWithLeastDeviation[T any, V any](array []T, getDeviation func(first T, second T) *uint64, returnFunc func(val T) V) *V {
	var consideredValue *T = nil
	var consideredAvgDeviation uint64 = math.MaxUint64

	for _, entry := range array {
		avgDeviation := new(Avg)
		for _, entry2 := range array {
			deviation := getDeviation(entry, entry2)
			if deviation != nil {
				avgDeviation.add(*deviation)
			}
		}

		if avgDeviation.isValid() {
			if avgDeviation.get() < consideredAvgDeviation {
				consideredValue = &entry
				consideredAvgDeviation = avgDeviation.get()
			}
		} else {
			consideredValue = &entry
			// keep consideredAvgDeviation at max value
		}
	}

	if consideredValue != nil {
		val := returnFunc(*consideredValue)
		return &val
	}
	return nil
}

// diff returns the difference between two numbers.
func diff(first uint64, second uint64) uint64 {
	if first > second {
		return first - second
	} else {
		return second - first
	}
}

type Avg struct {
	sum   uint64
	count uint64
}

func (avg Avg) add(value uint64) {
	avg.sum += value
	avg.count++
}

func (avg Avg) get() uint64 {
	if avg.count == 0 {
		return 0
	}

	return avg.sum / avg.count
}

func (avg Avg) isValid() bool {
	return avg.count != 0
}

var db = createDb()

func createDb() *bolt.DB {
	db, err := bolt.Open("server.db", 0600, nil)
	if err != nil {
		log.Fatal(err)
	}

	_ = db.Update(func(tx *bolt.Tx) error {
		b, err := tx.CreateBucketIfNotExists([]byte("leaderboard"))
		if err != nil {
			return fmt.Errorf("create bucket: %s", err)
		}

		_, err = b.CreateBucketIfNotExists([]byte("scores"))
		if err != nil {
			return fmt.Errorf("create bucket: %s", err)
		}

		_, err = b.CreateBucketIfNotExists([]byte("emojis"))
		if err != nil {
			return fmt.Errorf("create bucket: %s", err)
		}

		_, err = b.CreateBucketIfNotExists([]byte("emoji_timestamps"))
		if err != nil {
			return fmt.Errorf("create bucket: %s", err)
		}
		return nil
	})

	return db
}

func Beautify(num uint32) string {
	str := fmt.Sprintf("%d", num%1000)
	num /= 1000

	for num > 0 {
		str = fmt.Sprintf("%d.%s", num, str)
		num /= 1000
	}

	return str
}

func ReportBug(user string, data ...any) {
	req, _ := http.NewRequest("POST", "https://grieferutils.l3g7.dev/v3/bug_report", bytes.NewBuffer([]byte(fmt.Sprint(data...))))
	req.Header.Set("User-Agent", "GrieferUtils Server v"+VERSION)
	req.Header.Set("Content-Type", "text/plain")
	req.Header.Set("X-MINECRAFT-UUID", user)

	_, _ = http.DefaultClient.Do(req)
}
