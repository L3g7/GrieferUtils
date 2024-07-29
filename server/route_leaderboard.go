package main

import (
	"bytes"
	"encoding/base64"
	"encoding/binary"
	"encoding/json"
	"fmt"
	"github.com/golang-jwt/jwt"
	"github.com/sourcegraph/conc/pool"
	bolt "go.etcd.io/bbolt"
	"math"
	"net/http"
	"os"
	"strconv"
	"strings"
	"time"
)

const UnknownSkinEmoji uint64 = 1188976812928278638

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

// lastSync stores when the database was last synchronized with Discord, see syncEmojis.
var lastSync = int64(0)

// lastUpdate stores when the entries for the Discord leaderboard were last gathered.
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

	// Retrieve score and calculate position
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
	err := Decode(r.Body, &request)
	if err != nil {
		return err
	}

	// Calculate and save score
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

	// Trigger update if last one was over 30 minutes ago
	if time.Now().Unix()-lastUpdate > 60*30 {
		lastUpdate = math.MaxInt64 // Don't call updateLeaderboard twice
		go updateLeaderboard()
	}

	// Trigger sync if last one was over 24 hours ago
	if time.Now().Unix()-lastSync > 3600*24 {
		lastSync = math.MaxInt64 // Don't call syncEmojis twice
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

	// Update lastUpdate
	lastUpdate = time.Now().Unix()

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

	// Load existing emojis
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

	// Generate missing emojis
	syncPool := pool.New()
	for _, idx := range missingEmojis {
		i := idx
		entry := leaderboard[idx]
		syncPool.Go(func() {
			emojiId := resolveEmoji(entry.key)
			entry.emojiId = emojiId
			leaderboard[i].emojiId = emojiId
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
			leaderboard[i].name = resolveName(entry.key)
		})
	}
	syncPool.Wait()

	// Create message
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
			Name:  fmt.Sprintf("\u200b \u200b %d. <:tinyurl_com_2vxtwcec___:%d> __%s:__", position, entry.emojiId, strings.ReplaceAll(entry.name, "_", "\\_")),
			Value: fmt.Sprintf("\u200b \u200b \u200b \u200b \u200b \u200b \u200b \u200b \u200b \u200b \u200b \u200b \u200b %s", Beautify(entry.score)),
		})
	}

	// Send message
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

// resolveEmoji retrieves a player skin and stores it as a Discord emoji, returning its id.
func resolveEmoji(uuid []byte) uint64 {
	// Request skin
	imgRes, err := http.Get(fmt.Sprintf("https://render.skinmc.net/3d.php?user=%s&vr=0&hr0&aa=false&hrh=0&headOnly=true&ratio=9", string(uuid)))
	if err != nil {
		return UnknownSkinEmoji
	}

	img := new(bytes.Buffer)
	_, err = img.ReadFrom(imgRes.Body)
	if err != nil {
		return UnknownSkinEmoji
	}

	// Upload skin to discord
	imgB64 := base64.StdEncoding.EncodeToString(img.Bytes())
	req, err := http.NewRequest("POST", "https://discord.com/api/v10/guilds/1188280298631336007/emojis", bytes.NewBuffer([]byte(fmt.Sprintf("{"+
		"\"name\":\"tinyurl_com_2vxtwcec___\",\"image\":\"data:image/png;base64,%s\",\"roles\":[]}", imgB64))))
	if err != nil {
		return UnknownSkinEmoji
	}

	req.Header.Set("Authorization", "Bot "+os.Getenv("DISCORD_TOKEN"))
	req.Header.Set("Content-Type", "application/json")

	dcRes, err := http.DefaultClient.Do(req)
	if err != nil {
		return UnknownSkinEmoji
	}

	// Parse response
	var emoji Emoji
	err = DecodeLossy(dcRes.Body, &emoji)
	if err != nil {
		return UnknownSkinEmoji
	}

	emojiId, err := strconv.ParseUint(emoji.Id, 10, 64)
	if err != nil {
		return UnknownSkinEmoji
	}

	// Save emoji in database
	_ = db.Update(func(tx *bolt.Tx) error {
		b := tx.Bucket([]byte("leaderboard"))
		emojis := b.Bucket([]byte("emojis"))
		timestamps := b.Bucket([]byte("emoji_timestamps"))
		_ = emojis.Put(uuid, binary.LittleEndian.AppendUint64([]byte{}, emojiId))
		_ = timestamps.Put(uuid, binary.LittleEndian.AppendUint64([]byte{}, uint64(time.Now().Add(time.Hour*24).Unix())))

		return nil
	})

	return emojiId
}

// resolveName retrieves and returns the display name of a player.
func resolveName(uuid []byte) string {
	res, err := http.Get(fmt.Sprintf("https://sessionserver.mojang.com/session/minecraft/profile/%s", string(uuid)))
	if err != nil {
		return string(uuid)
	}

	profile := MinecraftProfile{}
	err = DecodeLossy(res.Body, &profile)
	if err != nil {
		return string(uuid)
	}

	return profile.Name
}

// syncEmojis synchronizes the database to reflect any changes made in Discord.
func syncEmojis() {
	// Retrieve Discord's emojis
	req, _ := http.NewRequest("GET", "https://discord.com/api/v10/guilds/1188280298631336007/emojis", nil)
	req.Header.Set("Authorization", "Bot "+os.Getenv("DISCORD_TOKEN"))

	res, err := http.DefaultClient.Do(req)
	if err != nil {
		return
	}

	var emojis []Emoji
	err = DecodeLossy(res.Body, &emojis)
	if err != nil {
		return
	}

	var emojisToDelete = append([]Emoji{}, emojis...)

	// Compare to emojis in database
	_ = db.Update(func(tx *bolt.Tx) error {
		b := tx.Bucket([]byte("leaderboard"))
		dbEmojis := b.Bucket([]byte("emojis"))
		timestamps := b.Bucket([]byte("emoji_timestamps"))

		_ = dbEmojis.ForEach(func(k, v []byte) error {
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
				return timestamps.Delete(k)
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

			// Don't delete emoji
			emojisToDelete = Remove(emojisToDelete, func(emoji Emoji) bool {
				return emoji.Id == discordEmoji.Id
			})
			return nil
		})

		return nil
	})

	// Delete flagged emojis
	syncPool := pool.New()
	for _, emoji := range emojisToDelete {
		syncPool.Go(func() {
			req, _ := http.NewRequest("DELETE", fmt.Sprintf("https://discord.com/api/v10/guilds/1188280298631336007/emojis/%s", emoji.Id), nil)
			req.Header.Set("Authorization", "Bot "+os.Getenv("DISCORD_TOKEN"))
			_, _ = http.DefaultClient.Do(req)
		})
	}
	syncPool.Wait()

	lastSync = time.Now().Unix()
}
