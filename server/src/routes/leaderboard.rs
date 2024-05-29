use std::sync::atomic::AtomicU64;
use std::sync::atomic::Ordering::Relaxed;
use std::time::{SystemTime, UNIX_EPOCH};

use base64::Engine;
use base64::prelude::BASE64_STANDARD;
use jammdb::DB;
use rocket::serde::{Deserialize, Serialize};
use rocket::serde::json::Json;
use rocket::State;
use rocket::time::ext::NumericalDuration;
use rocket::tokio::task::spawn;

use crate::{Constants, db};
use crate::auth::User;
use crate::error_handler::check_error;
use crate::util::SortedLimitedVec;

#[derive(Deserialize)]
#[serde(deny_unknown_fields)]
pub struct Request {
    flown: bool,
}

#[derive(Serialize)]
pub struct Response {
    score: u32,
    position: u32,
    #[serde(skip_serializing_if = "Option::is_none")]
    previous: Option<ResponseRelEntry>,
    #[serde(skip_serializing_if = "Option::is_none")]
    next: Option<ResponseRelEntry>,
}

#[derive(Serialize)]
pub struct ResponseRelEntry {
    uuid: String,
    score: u32,
}

#[derive(Default)]
pub struct LeaderboardData {
    /// Unix time when the leaderboard was last updated.
    last_update: AtomicU64,
    /// Unix time when the player emojis in the leaderboard were last synchronized.
    /// This sync is to make sure their skins are up-to-date.
    last_sync: AtomicU64,
}

fn parse_u32(data: &[u8]) -> u32 {
    u32::from_le_bytes(data.try_into().unwrap())
}

fn parse_u64(data: &[u8]) -> u64 {
    u64::from_le_bytes(data.try_into().unwrap())
}

#[get("/leaderboard")]
pub fn get_leaderboard(user: User, db: &State<DB>) -> Option<Json<Response>> {
    let tx = db.tx(false).ok()?;
    let leaderboard = tx.get_bucket("leaderboard").ok()?;
    let scores = leaderboard.get_bucket("scores").ok()?;

    let mut response = Response {
        position: 1,
        score: scores.get_kv(&user.uuid).map(|v| parse_u32(v.value())).unwrap_or(0),
        previous: None,
        next: None,
    };

    for kv in scores.kv_pairs() {
        let score = parse_u32(kv.value());

        // Find own score
        if user.uuid.as_bytes().eq(kv.key()) {
            response.score = score;
        } else {
            // Update position data
            if response.score < score {
                response.position += 1;
                // Check next
                if response.next.as_ref().map(|v| v.score).unwrap_or(u32::MAX) > score {
                    response.next = Some(ResponseRelEntry {
                        uuid: String::from_utf8(kv.key().to_vec()).unwrap(),
                        score,
                    });
                }
            } else if response.score != score {
                // Check previous
                if response.previous.as_ref().map(|v| v.score).unwrap_or(0) < score {
                    response.previous = Some(ResponseRelEntry {
                        uuid: String::from_utf8(kv.key().to_vec()).unwrap(),
                        score,
                    });
                }
            }
        }
    }

    db::commit(tx);

    Some(Json(response))
}

#[post("/leaderboard", format = "json", data = "<request>")]
pub fn post_leaderboard(user: User, db: &State<DB>, state: &State<LeaderboardData>, constants: &State<Constants>, request: Json<Request>) -> Option<Json<Response>> {
    let tx = db.tx(true).ok()?;
    let leaderboard = tx.get_bucket("leaderboard").ok()?;
    let scores = leaderboard.get_bucket("scores").ok()?;

    // Get new score
    let mut score = scores.get_kv(&user.uuid).map(|v| parse_u32(v.value())).unwrap_or(0);
    if request.flown {
        score += 1;
    } else {
        score += 2;
    }

    // Update new score
    let _ = scores.put(user.uuid.clone(), score.to_le_bytes());

    let now = SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_secs();

    // Update leaderboard if over 30 min old
    if now - state.last_update.load(Relaxed) > 60 * 30 {
        state.last_update.store(now, Relaxed);
        spawn(update_leaderboard(db.inner().clone(), constants.discord_token.to_owned()));
    }

    // Synchronize if last sync was over 24 hours ago
    else if now - state.last_sync.load(Relaxed) > 3600 * 24 {
        state.last_sync.store(now, Relaxed);
        spawn(sync_emojis(db.inner().clone(), constants.discord_token.to_owned()));
    }

    // Return new leaderboard data
    get_leaderboard(user, db)
}

async fn update_leaderboard(db: DB, discord_token: String) -> Option<()> {
    let mut leaderboard = SortedLimitedVec::new(10, |v: &LeaderboardEntry| v.score);
    {
        let tx = db.tx(false).ok()?;
        let b_leaderboard = tx.get_bucket("leaderboard").ok()?;

        // Get leaderboard
        let scores = b_leaderboard.get_bucket("scores").ok()?;

        for kv in scores.kv_pairs() {
            leaderboard.push(LeaderboardEntry {
                key: kv.key().to_vec(),
                name: "".to_string(),
                score: parse_u32(kv.value()),
                emoji_id: 0,
            });
        }

        db::commit(tx);
    }
    let mut leaderboard = leaderboard.to_vec();
    {
        // Delete expired emojis
        let tx = db.tx(false).ok()?;
        let b_leaderboard = tx.get_bucket("leaderboard").ok()?;
        let emojis = b_leaderboard.get_bucket("emojis").ok()?;
        let emoji_timestamps = b_leaderboard.get_bucket("emoji_timestamps").ok()?;

        let now = SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_secs();
        for kv in emoji_timestamps.kv_pairs() {
            if now > parse_u64(kv.value()) {
                // Delete emoji
                emojis.delete(kv.key()).ok()?;
                emoji_timestamps.delete(kv.key()).ok()?;
            }
        }

        // Load emojis
        for entry in &mut leaderboard {
            if let Some(kv) = emojis.get_kv(&entry.key) {
                entry.emoji_id = parse_u64(kv.value());
                emoji_timestamps.put(entry.key.clone(), now.to_le_bytes()).ok()?;
            }
        }

        // Claim space for missing emojis
        let missing_emoji_count = leaderboard.iter().filter(|e| e.emoji_id == 0).count();
        let existing_emoji_count = emojis.next_int() as usize;
        if missing_emoji_count + existing_emoji_count > 50 {
            let count_to_remove = missing_emoji_count + existing_emoji_count - 50;
            // Remove oldest emojis
            let mut emojis_to_remove = SortedLimitedVec::new(count_to_remove, |v: &(Vec<u8>, u64)| -i64::try_from(v.1).unwrap());
            for kv in emoji_timestamps.kv_pairs() {
                emojis_to_remove.push((kv.key().to_vec(), parse_u64(kv.value())));
            }

            for emoji in emojis_to_remove.to_vec() {
                emojis.delete(&emoji.0).ok()?;
                emoji_timestamps.delete(emoji.0).ok()?;
            }
        }

        db::commit(tx);
    }

    // Resolve details
    let mut changed_emojis: Vec<(Vec<u8>, u64, u64)> = vec![];
    for entry in leaderboard.iter_mut() {
        // Resolve username
        entry.name = resolve_username(&entry.key).await?;

        // Resolve emoji
        if entry.emoji_id == 0 {
            let emoji_id = resolve_emoji(&entry.key, &discord_token).await;
            entry.emoji_id = emoji_id.unwrap_or(1188976812928278638);
            let emoji_id = emoji_id.unwrap_or(0);
            let expiration_date = SystemTime::now() + 24.hours();
            let exp_unix = expiration_date.duration_since(UNIX_EPOCH).unwrap().as_secs();
            changed_emojis.push((entry.key.to_owned(), emoji_id, exp_unix));
        }
    }

    {
        let tx = db.tx(false).ok()?;
        let b_leaderboard = tx.get_bucket("leaderboard").ok()?;
        let emojis = b_leaderboard.get_bucket("emojis").ok()?;
        let emoji_timestamps = b_leaderboard.get_bucket("emoji_timestamps").ok()?;
        for (key, id, ts) in changed_emojis {
            let _ = emojis.put(key.clone(), id.to_le_bytes());
            let _ = emoji_timestamps.put(key, ts.to_le_bytes());
        }
        db::commit(tx);
    }

    // Create embed
    let mut embed = DiscordEmbed {
        title: "🏆 **__Spawn-Runden Zähler Leaderboard__**",
        description: format!("Gelaufene Runden zählen `2x`,\
        geflogene Runden `1x`.\
        Letztes Update: <t:{}:R>\n​", SystemTime::UNIX_EPOCH.elapsed().unwrap().as_secs()),
        color: 14922248,
        fields: vec![],
    };

    let mut position = 0;
    let mut last_score = u32::MAX;
    for (idx, entry) in leaderboard.iter().enumerate() {
        if entry.score == 0 {
            break;
        }

        if entry.score < last_score {
            position = idx + 1;
            last_score = entry.score;
        }

        embed.fields.push(DiscordField {
            name: format!("​ ​ {}. <:tinyurl_com_2vxtwcec___:{}> __{}:__", position, entry.emoji_id, entry.name.replace("_", "\\_")),
            value: format!("​ ​ ​ ​ ​ ​ ​ ​ ​ ​ ​ ​ ​ {}", beautify_num(entry.score)),
        });
    }

    let client = reqwest::Client::new();

    client.patch("https://discord.com/api/v10/channels/1233417975281877074/messages/1233418579014062080")
        .json(&DiscordMessage { embeds: [embed] })
        .header("Authorization", format!("Bot {}", discord_token))
        .send().await.ok()?;

    Some(())
}

async fn resolve_username(uuid: &Vec<u8>) -> Option<String> {
    let uuid = std::str::from_utf8(uuid).unwrap();
    let client = reqwest::Client::new();

    #[derive(Deserialize)]
    struct MinecraftProfile {
        name: String,
    }

    let res: MinecraftProfile = client.get(format!("https://sessionserver.mojang.com/session/minecraft/profile/{}", uuid))
        .send().await.ok()?
        .json().await.ok()?;

    Some(res.name)
}

async fn resolve_emoji(uuid: &Vec<u8>, discord_token: &str) -> Option<u64> {
    let uuid = std::str::from_utf8(uuid).unwrap();
    let client = reqwest::Client::new();

    // Render image
    let bytes = client.get(format!("https://render.skinmc.net/3d.php?user={}&vr=0&hr0&aa=false&hrh=0&headOnly=true&ratio=9", uuid))
        .send().await.ok()?
        .bytes().await.ok()?;

    #[derive(Deserialize)]
    struct DiscordEmoji {
        id: String,
    }

    // Upload image
    let image = BASE64_STANDARD.encode(bytes);
    let res: DiscordEmoji = client.post("https://discord.com/api/v10/guilds/1188280298631336007/emojis")
        .body(format!(r#"{{"name":"tinyurl_com_2vxtwcec___","image":"data:image/png;base64,{}","roles":[]}}"#, image))
        .header("Authorization", format!("Bot {}", discord_token))
        .header("Content-Type", "application/json")
        .send().await.ok()?
        .json().await.ok()?;

    res.id.parse().ok()
}

async fn sync_emojis(db: DB, discord_token: String) -> Option<()> {
    let client = reqwest::Client::new();

    #[derive(Deserialize, Clone)]
    struct DiscordEmoji {
        id: u64,
    }

    // Upload image
    let dc_emojis: Vec<DiscordEmoji> = client.get("https://discord.com/api/v10/guilds/1188280298631336007/emojis")
        .header("Authorization", format!("Bot {}", discord_token))
        .send().await.ok()?
        .json().await.ok()?;

    let mut emojis_to_delete = dc_emojis.to_vec();
    if let Some(tx) = db::tx(&db, true) {
        let leaderboard_bucket = check_error(tx.get_or_create_bucket("leaderboard"))?;
        let emojis = check_error(leaderboard_bucket.get_or_create_bucket("emojis"))?;
        let timestamps = check_error(leaderboard_bucket.get_or_create_bucket("emoji_timestamps"))?;

        for emoji in emojis.kv_pairs() {
            let id = parse_u64(emoji.value());

            // Find matching Discord emoji
            let mut matching_emoji = None;
            for (i, emoji) in dc_emojis.iter().enumerate() {
                if emoji.id == id {
                    matching_emoji = Some(i);
                    break;
                }
            }

            if matching_emoji.is_none() {
                // Emoji exists in db but not in Discord
                // Delete from db
                let _ = emojis.delete(emoji.key());
                let _ = timestamps.delete(emoji.key());
                continue;
            }

            // Check if emoji is expired
            let end = timestamps.get_kv(emoji.key())
                .map(|kv| parse_u64(kv.value()))
                .unwrap_or(0);
            if SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_secs() > end {
                // Delete from database
                let _ = emojis.delete(emoji.key());
                let _ = timestamps.delete(emoji.key());
                continue;
            }

            emojis_to_delete.swap_remove(matching_emoji.unwrap());
        }
        db::commit(tx);
    }

    for emoji in emojis_to_delete {
        let _ = client.delete(format!("https://discord.com/api/v10/guilds/1188280298631336007/emojis/{}", emoji.id))
            .header("Authorization", format!("Bot {}", discord_token))
            .send().await;
    }

    Some(())
}

fn beautify_num(mut num: u32) -> String {
    let mut str = format!("{:0>8}", num % 1000);

    num /= 1000;
    while num > 0 {
        str = format!("{:0>8}.{}", num % 1000, str);
        num /= 1000;
    }

    str.trim_start_matches("0").to_string()
}

#[derive(Default)]
struct LeaderboardEntry {
    key: Vec<u8>,
    name: String,
    score: u32,
    emoji_id: u64,
}

#[derive(Serialize)]
struct DiscordField {
    name: String,
    value: String,
}

#[derive(Serialize)]
struct DiscordEmbed<'a> {
    title: &'a str,
    description: String,
    color: u32,
    fields: Vec<DiscordField>,
}

#[derive(Serialize)]
struct DiscordMessage<'a> {
    embeds: [DiscordEmbed<'a>; 1],
}
