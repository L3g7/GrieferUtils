package main

import (
	"bytes"
	"encoding/base64"
	"encoding/binary"
	"encoding/json"
	"fmt"
	"github.com/boltdb/bolt"
	"github.com/golang-jwt/jwt"
	"github.com/sourcegraph/conc/pool"
	"net/http"
	"os"
	"strconv"
	"time"
)

type LeaderboardRequest struct {
	flown bool
}

type Emoji struct {
	Id string `json:"id"`
}

type DiscordField struct {
	Name   string `json:"name"`
	Value  string `json:"value"`
	Inline bool   `json:"inline"`
}

type MinecraftProfile struct {
	Name string `json:"name"`
}

type LeaderboardEntry struct {
	key     []byte
	name    string
	score   uint32
	emojiId uint64
}

var lastSync = int64(0)
var lastUpdate = lastSync

func LeaderboardRoute(w http.ResponseWriter, r *http.Request, token *jwt.Token) error {
	if r.Method == "GET" {
		return LeaderboardGetRoute(w, token)
	} else if r.Method == "POST" {
		return LeaderboardPostRoute(w, r, token)
	} else {
		Error(w, http.StatusMethodNotAllowed, "Method Not Allowed")
	}
	return nil
}

func LeaderboardGetRoute(w http.ResponseWriter, token *jwt.Token) error {
	claims, _ := token.Claims.(jwt.MapClaims)
	user := []byte(claims["sub"].(string))

	score := uint32(0)
	err := db.View(func(tx *bolt.Tx) error {
		b := tx.Bucket([]byte("leaderboard"))
		scores := b.Bucket([]byte("scores"))

		if value := scores.Get(user); value != nil {
			score = binary.LittleEndian.Uint32(value)
		}

		return nil
	})

	_, _ = fmt.Fprintf(w, "{\"score\":%d}", score)
	return err
}

func LeaderboardPostRoute(w http.ResponseWriter, r *http.Request, token *jwt.Token) error {
	if r.Header.Get("Content-Type") != "application/json" {
		Error(w, http.StatusUnsupportedMediaType, "Unsupported Media Type")
		return nil
	}

	claims, _ := token.Claims.(jwt.MapClaims)
	user := claims["sub"].(string)

	// Parse request
	var request LeaderboardRequest
	err := DecodeFully(r.Body, &request)
	if err != nil {
		return err
	}

	err = db.Update(func(tx *bolt.Tx) error {
		b := tx.Bucket([]byte("leaderboard"))
		scores := b.Bucket([]byte("scores"))

		var score uint32 = 0
		if value := scores.Get([]byte(user)); value != nil {
			score = binary.LittleEndian.Uint32(value)
		}

		return scores.Put([]byte(user), binary.LittleEndian.AppendUint32([]byte{}, score+1))
	})
	if err != nil {
		return err
	}

	// Check if last update was over 30 minutes ago
	if time.Now().Unix()-lastUpdate > 60*30 {
		go updateLeaderboard()
	}

	if time.Now().Unix()-lastSync > 3600*24 {
		go syncEmojis()
	}

	_, _ = fmt.Fprint(w, "{}")
	return nil
}

func updateLeaderboard() {
	// Get leaderboard
	var leaderboard = make([]LeaderboardEntry, 10)
	_ = db.View(func(tx *bolt.Tx) error {
		b := tx.Bucket([]byte("leaderboard"))
		scores := b.Bucket([]byte("scores"))
		return scores.ForEach(func(k, v []byte) error {
			score := binary.LittleEndian.Uint32(v)

			var idx = -1
			for i := 9; i >= 0; i-- {
				if leaderboard[i].score < score {
					idx = i
				} else {
					break
				}
			}

			if idx != -1 {
				smallerPart := leaderboard[idx+1 : 9]
				leaderboard = append(leaderboard[:idx], LeaderboardEntry{
					key:   k,
					score: score,
				})
				leaderboard = append(leaderboard, smallerPart...)
			}

			return nil
		})
	})

	// Delete expired emojis
	_ = db.Update(func(tx *bolt.Tx) error {
		b := tx.Bucket([]byte("leaderboard"))
		emojis := b.Bucket([]byte("emojis"))
		timestamps := b.Bucket([]byte("emoji_timestamps"))

		// Check if expired
		return timestamps.ForEach(func(k, v []byte) error {
			if time.Now().Unix() > int64(binary.LittleEndian.Uint64(v)) {
				// Delete from database
				err := emojis.Delete(k)
				if err != nil {
					return err
				}
				return timestamps.Delete(k)
			}
			return nil
		})
	})

	// Load emojis
	var missingEmojis []*LeaderboardEntry
	var emojiCount int
	for _, entry := range leaderboard {
		_ = db.View(func(tx *bolt.Tx) error {
			b := tx.Bucket([]byte("leaderboard"))
			emojis := b.Bucket([]byte("emojis"))
			emojiCount = emojis.Stats().KeyN

			// Check if Emoji expired
			id := emojis.Get(entry.key)
			if id != nil {
				entry.emojiId = binary.LittleEndian.Uint64(id)
			}
			return nil
		})
		if entry.emojiId == 0 {
			missingEmojis = append(missingEmojis, &entry)
		}
	}

	// Claim space for missing emojis
	if len(missingEmojis)+emojiCount > 50 {
		_ = db.Update(func(tx *bolt.Tx) error {
			b := tx.Bucket([]byte("leaderboard"))
			emojis := b.Bucket([]byte("emojis"))

			toRemove := len(missingEmojis) + emojiCount - 50

			c := emojis.Cursor()

		emojiIt:
			for k, v := c.First(); k != nil && toRemove > 0; k, v = c.Next() {
				id := binary.LittleEndian.Uint64(v)
				// Check if Emoji is required
				for _, entry := range leaderboard {
					if entry.emojiId == id {
						continue emojiIt
					}
				}

				// Delete Emoji
				_ = c.Delete()
				toRemove--
			}

			return nil
		})
		syncEmojis()
	}

	syncPool := pool.New()
	for _, entry := range missingEmojis {
		syncPool.Go(func() {
			imgRes, _ := http.Get(fmt.Sprintf("https://render.skinmc.net/3d.php?user=%s&vr=0&hr0&aa=false&hrh=0&headOnly=true&ratio=9", string(entry.key)))

			img := new(bytes.Buffer)
			_, _ = img.ReadFrom(imgRes.Body)

			imgb64 := base64.StdEncoding.EncodeToString(img.Bytes())
			req, _ := http.NewRequest("POST", "https://discord.com/api/v10/guilds/1188280298631336007/emojis", bytes.NewBuffer([]byte(fmt.Sprintf("{"+
				"\"name\":\"tinyurl_com_2vxtwcec\",\"image\":\"data:image/png;base64,%s\",\"roles\":[]}", imgb64))))
			req.Header.Set("Authorization", "Bot "+os.Getenv("DISCORD_TOKEN"))
			req.Header.Set("Content-Type", "application/json")

			dcRes, _ := http.DefaultClient.Do(req)
			var emoji Emoji
			err := Decode(dcRes.Body, &emoji)
			if err != nil {
				emoji.Id = "1188976812928278638"
			}

			entry.emojiId, _ = strconv.ParseUint(emoji.Id, 10, 64)
			_ = db.Update(func(tx *bolt.Tx) error {
				b := tx.Bucket([]byte("leaderboard"))
				emojis := b.Bucket([]byte("emojis"))
				timestamps := b.Bucket([]byte("emoji_timestamps"))
				_ = emojis.Put(entry.key, binary.LittleEndian.AppendUint64([]byte{}, entry.emojiId))
				_ = timestamps.Put(entry.key, binary.LittleEndian.AppendUint64([]byte{}, uint64(time.Now().Add(time.Hour*24).Unix())))

				return nil
			})
		})
	}
	syncPool.Wait()
	go func() {
		var fields []DiscordField

		// Get usernames
		for _, entry := range leaderboard {
			syncPool.Go(func() {
				res, _ := http.Get(fmt.Sprintf("https://sessionserver.mojang.com/session/minecraft/profile/%s", string(entry.key)))
				var profile MinecraftProfile
				err := Decode(res.Body, &profile)
				if err == nil {
					entry.name = profile.Name
				} else {
					entry.name = string(entry.key)
					ReportBug(string(entry.key), res, err)
				}
			})
		}
		syncPool.Wait()

		for i, entry := range leaderboard {
			fields = append(fields, DiscordField{
				Name:  fmt.Sprintf("\u200b \u200b %d. <:_:%d> __%s:__", i, entry.emojiId, entry.name),
				Value: fmt.Sprintf("\u200b \u200b \u200b \u200b \u200b \u200b \u200b \u200b \u200b \u200b \u200b \u200b \u200b %s", Beautify(entry.score)),
			})
		}

		b, _ := json.Marshal(fields)
		req, _ := http.NewRequest("PATCH", "https://discord.com/api/v10/channels/1185718903805067324/messages/1188298133193629726", bytes.NewBuffer([]byte(`
{
    "embeds": [
        {
            "title": "\ud83c\udfc6 **__Spawn-Runden Z\u00e4hler Leaderboard__**",
            "description": "Gelaufene Runden z\u00e4hlen \u00602x\u0060,\ngeflogene Runden \u00601x\u0060.\n\u200b",
            "color": 14922248,
            "fields": `+string(b)+`
        }
    ]
}`)))
		req.Header.Set("Authorization", "Bot "+os.Getenv("DISCORD_TOKEN"))
		req.Header.Set("Content-Type", "application/json")

		_, _ = http.DefaultClient.Do(req)
	}()
	lastUpdate = time.Now().Unix()

}

func syncEmojis() int64 {
	req, _ := http.NewRequest("GET", "https://discord.com/api/v10/guilds/1188280298631336007/emojis", nil)
	req.Header.Set("Authorization", "Bot "+os.Getenv("DISCORD_TOKEN"))

	res, _ := http.DefaultClient.Do(req)

	var emojis []Emoji
	err := Decode(res.Body, &emojis)
	if err != nil {
		ReportBug("syncEmojis() GET emojis", err)
		return time.Now().Unix()
	}

	var emojisToDelete = append([]Emoji{}, emojis...)

	err = db.Update(func(tx *bolt.Tx) error {
		b := tx.Bucket([]byte("leaderboard"))
		dbEmojis := b.Bucket([]byte("emojis"))
		timestamps := b.Bucket([]byte("emoji_timestamps"))

		// Validate dbEmojis
		err := dbEmojis.ForEach(func(k, v []byte) error {
			id := binary.LittleEndian.Uint64(v)

			// Check if discord Emoji exists
			var discordEmoji *Emoji
			for _, emoji := range emojis {
				if emoji.Id == strconv.FormatUint(id, 10) {
					discordEmoji = &emoji
					break
				}
			}

			if discordEmoji == nil {
				// Delete from database
				err := dbEmojis.Delete(k)
				if err != nil {
					return err
				}
				err = timestamps.Delete(k)
				if err != nil {
					return err
				}
				return nil
			}

			// Check if expired
			end := binary.LittleEndian.Uint64(timestamps.Get(k))
			if time.Now().Unix() > int64(end) {
				// Delete from database
				err := dbEmojis.Delete(k)
				if err != nil {
					return err
				}
				err = timestamps.Delete(k)
				if err != nil {
					return err
				}
			}

			emojisToDelete = Remove(emojisToDelete, func(emoji Emoji) bool {
				return emoji.Id == discordEmoji.Id
			})
			return nil
		})
		if err != nil {
			return err
		}

		return nil
	})
	if err != nil {
		ReportBug("syncEmojis() db update", err)
		return time.Now().Unix()
	}

	// delete all flagged emojis
	syncPool := pool.New()
	for _, emoji := range emojisToDelete {
		syncPool.Go(func() {
			req, _ := http.NewRequest("DELETE", fmt.Sprintf("https://discord.com/api/v10/guilds/1188280298631336007/emojis/%s", emoji.Id), nil)
			req.Header.Set("Authorization", "Bot "+os.Getenv("DISCORD_TOKEN"))
			res, _ = http.DefaultClient.Do(req)
			if res.StatusCode != 204 {
				ReportBug("syncEmojis() DELETE", res.StatusCode, res.Body)
			}
		})
	}
	syncPool.Wait()

	return time.Now().Unix()
}
