package main

import (
	"encoding/json"
	"errors"
	"io"
	"math"
)

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
		var deviationSum uint64 = 0
		var deviationCount uint64 = 0
		for _, entry2 := range array {
			deviation := getDeviation(entry, entry2)
			if deviation != nil {
				deviationSum += *deviation
				deviationCount++
			}
		}

		if deviationCount == 0 {
			consideredValue = &entry
			// keep consideredAvgDeviation at max value
		} else {
			avgDeviation := deviationSum / deviationCount

			if avgDeviation < consideredAvgDeviation {
				consideredValue = &entry
				consideredAvgDeviation = avgDeviation
			}
		}
	}

	if consideredValue != nil {
		val := returnFunc(*consideredValue)
		return &val
	}
	return nil
}
