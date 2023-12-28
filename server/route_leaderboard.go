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
	Flown bool `json:"flown"`
}

type LeaderboardResponse struct {
	Score    uint32                    `json:"score"`
	Position int                       `json:"position"`
	Previous *LeaderboardResponseEntry `json:"previous,omitempty"`
	Next     *LeaderboardResponseEntry `json:"next,omitempty"`
}

type LeaderboardResponseEntry struct {
	UUID  string `json:"uuid"`
	Score uint32 `json:"score"`
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

	response := LeaderboardResponse{
		Position: 1,
		Next: &LeaderboardResponseEntry{
			Score: 4294967295,
		},
		Previous: &LeaderboardResponseEntry{},
	}

	err := db.View(func(tx *bolt.Tx) error {
		b := tx.Bucket([]byte("leaderboard"))
		scores := b.Bucket([]byte("scores"))

		if value := scores.Get(user); value != nil {
			response.Score = binary.LittleEndian.Uint32(value)
		}

		return scores.ForEach(func(k, v []byte) error {
			score := binary.LittleEndian.Uint32(v)

			if response.Score < score {
				response.Position++

				if response.Next.Score > score {
					response.Next = &LeaderboardResponseEntry{
						UUID:  string(k),
						Score: score,
					}
				}
			} else if response.Score > score {
				if response.Previous.Score < score {
					response.Previous = &LeaderboardResponseEntry{
						UUID:  string(k),
						Score: score,
					}
				}
			}

			return nil
		})
	})

	if response.Next.UUID == "" {
		response.Next = nil
	}
	if response.Previous.UUID == "" {
		response.Previous = nil
	}

	_ = json.NewEncoder(w).Encode(response)
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

	var score uint32 = 0
	err = db.Update(func(tx *bolt.Tx) error {
		b := tx.Bucket([]byte("leaderboard"))
		scores := b.Bucket([]byte("scores"))

		if value := scores.Get([]byte(user)); value != nil {
			score = binary.LittleEndian.Uint32(value)
		}

		if request.Flown {
			score += 1
		} else {
			score += 2
		}
		return scores.Put([]byte(user), binary.LittleEndian.AppendUint32([]byte{}, score))
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

	return LeaderboardGetRoute(w, token)
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
				newLeaderboard := make([]LeaderboardEntry, idx)
				copy(newLeaderboard, leaderboard[:idx])
				newLeaderboard = append(newLeaderboard, LeaderboardEntry{
					key:   k,
					score: score,
				})
				leaderboard = append(newLeaderboard, leaderboard[idx:9]...)
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
	var missingEmojis []int
	var emojiCount int
	for i, entry := range leaderboard {
		if entry.score == 0 {
			break
		}

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
			missingEmojis = append(missingEmojis, i)
		}
		leaderboard[i].emojiId = entry.emojiId
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
	for _, i := range missingEmojis {
		entry := leaderboard[i]
		syncPool.Go(func() {
			imgRes, _ := http.Get(fmt.Sprintf("https://render.skinmc.net/3d.php?user=%s&vr=0&hr0&aa=false&hrh=0&headOnly=true&ratio=9", string(entry.key)))

			img := new(bytes.Buffer)
			_, _ = img.ReadFrom(imgRes.Body)

			imgb64 := base64.StdEncoding.EncodeToString(img.Bytes())
			req, _ := http.NewRequest("POST", "https://discord.com/api/v10/guilds/1188280298631336007/emojis", bytes.NewBuffer([]byte(fmt.Sprintf("{"+
				"\"name\":\"tinyurl_com_2vxtwcec___\",\"image\":\"data:image/png;base64,%s\",\"roles\":[]}", imgb64))))
			req.Header.Set("Authorization", "Bot "+os.Getenv("DISCORD_TOKEN"))
			req.Header.Set("Content-Type", "application/json")

			dcRes, _ := http.DefaultClient.Do(req)
			var emoji Emoji
			err := Decode(dcRes.Body, &emoji)
			if err != nil {
				emoji.Id = "1188976812928278638"
			}

			emojiId, _ := strconv.ParseUint(emoji.Id, 10, 64)
			entry.emojiId = emojiId
			leaderboard[i].emojiId = emojiId
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
	var fields []DiscordField

	// Get usernames
	syncPool = pool.New()
	for idx, lEntry := range leaderboard {
		if lEntry.score == 0 {
			break
		}

		i := idx
		entry := lEntry
		syncPool.Go(func() {
			res, err := http.Get(fmt.Sprintf("https://sessionserver.mojang.com/session/minecraft/profile/%s", string(entry.key)))
			if err != nil {
				leaderboard[i].name = string(entry.key)
				ReportBug(string(entry.key), err)
				return
			}

			profile := MinecraftProfile{}
			err = Decode(res.Body, &profile)
			if err == nil {
				leaderboard[i].name = profile.Name
			} else {
				leaderboard[i].name = string(entry.key)
				ReportBug(string(entry.key), res, err)
			}
		})
	}
	syncPool.Wait()

	position := 0
	var lastScore uint32 = 4294967295
	for i, entry := range leaderboard {
		if entry.score == 0 {
			break
		}

		if entry.score < lastScore {
			position = i + 1
			lastScore = entry.score
		}

		fields = append(fields, DiscordField{
			Name:  fmt.Sprintf("\u200b \u200b %d. <:_:%d> __%s:__", position, entry.emojiId, entry.name),
			Value: fmt.Sprintf("\u200b \u200b \u200b \u200b \u200b \u200b \u200b \u200b \u200b \u200b \u200b \u200b \u200b %s", Beautify(entry.score)),
		})
	}

	lastUpdate = time.Now().Unix()

	b, _ := json.Marshal(fields)
	req, _ := http.NewRequest("PATCH", "https://discord.com/api/v10/channels/1185718903805067324/messages/1188298133193629726", bytes.NewBuffer([]byte(fmt.Sprintf(`
{
    "embeds": [
        {
            "title": "\ud83c\udfc6 **__Spawn-Runden Z\u00e4hler Leaderboard__**",
            "description": "Gelaufene Runden z\u00e4hlen \u00602x\u0060,\ngeflogene Runden \u00601x\u0060.\nLetztes Update: <t:%d:R>\n\u200b",
            "color": 14922248,
            "fields": %s
        }
    ]
}`, lastUpdate, string(b)))))
	req.Header.Set("Authorization", "Bot "+os.Getenv("DISCORD_TOKEN"))
	req.Header.Set("Content-Type", "application/json")

	_, _ = http.DefaultClient.Do(req)

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
