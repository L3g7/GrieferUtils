use std::collections::HashMap;
use std::sync::Mutex;
use std::time::{Duration, SystemTime, UNIX_EPOCH};

use rocket::serde::{Deserialize, Serialize};
use rocket::serde::json::Json;
use rocket::State;
use rocket::time::ext::NumericalDuration;

use crate::Constants;
use crate::routes::hive_mind::util::{DeviatingRef, get_with_least_avg_deviation};
use crate::auth::User;
use crate::util::Empty;

#[derive(Default)]
pub struct MobRemoverData {
    /// {citybuild => {user => reported timestamp}}
    timestamps: Mutex<HashMap<String, HashMap<String, u64>>>,
}

impl DeviatingRef for u64 {
    fn deviation_to(&self, other: &Self) -> u64 {
        self.abs_diff(*other)
    }
}

#[derive(Deserialize)]
#[serde(deny_unknown_fields)]
pub struct Request {
    value: u64,
}

#[derive(Serialize)]
pub struct Response {
    #[serde(skip_serializing_if = "Option::is_none")]
    value: Option<u64>,
}

#[get("/<citybuild>")]
pub fn get_mob_remover(_user: User, data: &State<MobRemoverData>, constants: &State<Constants>, citybuild: String) -> Option<Json<Response>> {
    if !constants.valid_citybuilds.contains(&citybuild) {
        // Citybuild is invalid, trigger 400 Bad Request
        return None;
    }

    let mut timestamps = data.timestamps.lock().ok()?;
    let entries = timestamps.entry(citybuild.into())
        .or_insert_with(|| HashMap::new());

    let now = SystemTime::now().duration_since(UNIX_EPOCH).ok()?.as_secs();

    // Remove entries that already passed
    entries.retain(|_, v| now > *v);

    // Find timestamp
    let timestamp = get_with_least_avg_deviation(entries).map(|v| *v);
    Some(Json(Response { value: timestamp }))
}

#[post("/<citybuild>", format = "json", data = "<request>")]
pub fn post_mob_remover(user: User, data: &State<MobRemoverData>, constants: &State<Constants>, citybuild: String, request: Json<Request>) -> Option<Empty> {
    if !constants.valid_citybuilds.contains(&citybuild) {
        // Citybuild is invalid, trigger 400 Bad Request
        return None;
    }

    let timestamp_st = SystemTime::UNIX_EPOCH + Duration::from_secs(request.value);
    if SystemTime::now() + 20.minutes() < timestamp_st {
        // timestamp is more than 20 minutes away, trigger 400 Bad Request
        return None;
    }

    // Insert data
    let mut timestamps = data.timestamps.lock().unwrap();
    let entries = timestamps.entry(citybuild)
        .or_insert_with(|| HashMap::new());

    entries.insert(user.uuid, request.value);
    Some(Empty {})
}
