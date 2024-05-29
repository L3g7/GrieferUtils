use std::collections::HashMap;
use std::convert::Into;
use std::sync::Mutex;
use std::time::{SystemTime, UNIX_EPOCH};

use rocket::serde::{Deserialize, Serialize};
use rocket::serde::json::Json;
use rocket::State;

use crate::Constants;
use crate::routes::hive_mind::util::{DeviatingRef, get_with_least_avg_deviation};
use crate::auth::User;
use crate::routes::online_users::OnlineUsersData;
use crate::util::Empty;

const MAX_LEVELS_PER_TYPE: usize = 5;

#[derive(Serialize, Deserialize)]
#[serde(deny_unknown_fields)]
pub struct BoosterStates {
    #[serde(rename = "break")]
    #[serde(skip_serializing_if = "Vec::is_empty")]
    break_: Vec<u64>,
    #[serde(skip_serializing_if = "Vec::is_empty")]
    drop: Vec<u64>,
    #[serde(skip_serializing_if = "Vec::is_empty")]
    fly: Vec<u64>,
    #[serde(skip_serializing_if = "Vec::is_empty")]
    mob: Vec<u64>,
    #[serde(skip_serializing_if = "Vec::is_empty")]
    xp: Vec<u64>,
}

#[derive(Default)]
pub struct BoosterData {
    /// { citybuild => { booster type => { user => (add timestamp, exp dates) }}}
    exp_dates: Mutex<HashMap<String, HashMap<String, HashMap<String, (u64, Vec<u64>)>>>>,
}

impl DeviatingRef for &(u64, Vec<u64>) {
    fn deviation_to(&self, other: &Self) -> u64 {
        let mut dev = 0u64;
        for i in 0..self.1.len() {
            dev += self.1[i].abs_diff(other.1[i]);
        }
        dev
    }
}

#[derive(Serialize)]
pub struct Response {
    known: bool,

    #[serde(flatten)]
    state: BoosterStates,
}

#[get("/<citybuild>")]
pub fn get_booster(_user: User, data: &State<BoosterData>, online_users: &State<OnlineUsersData>, constants: &State<Constants>, citybuild: String) -> Option<Json<Response>> {
    if !constants.valid_citybuilds.contains(&citybuild) {
        // Citybuild is invalid, trigger 400 Bad Request
        return None;
    }

    let mut exp_dates = data.exp_dates.lock().unwrap();
    let citybuild_entries = exp_dates.entry(citybuild)
        .or_insert_with(|| HashMap::new());

    let now = SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_secs();
    let mut any_known = false;

    // Collect states
    let mut collect = |booster_type: &str| {
        let type_entries = citybuild_entries.entry(booster_type.into())
            .or_insert_with(|| HashMap::new());

        // Remove old expiration dates
        for x in type_entries.values_mut() {
            x.1.retain(|ts| now <= *ts);
        }

        // Remove entries with no dates left or where user is offline
        type_entries.retain(|name, ts| !ts.1.is_empty() && online_users.has(name));

        // Find expiration dates with the least average deviation for the level with the newest dates on average
        let mut considered_exp_dates: Option<&Vec<u64>> = None;
        let mut considered_avg_ts = u64::MAX;
        for level in 0..MAX_LEVELS_PER_TYPE {
            let level_entries: HashMap<&String, &(u64, Vec<u64>)> = type_entries.iter()
                .filter(|(_, (_, v))| v.len() == level)
                .collect();

            if level_entries.len() == 0 {
                continue;
            }

            any_known = true;
            let timestamps: Vec<u64> = level_entries.values().map(|v| v.0).collect();
            let avg_ts = timestamps.iter().sum::<u64>() / timestamps.len() as u64;

            // Check if data with is newer on average
            if avg_ts > considered_avg_ts {
                let exp_dates = get_with_least_avg_deviation(&level_entries).unwrap();
                considered_exp_dates = Some(&exp_dates.1);
                considered_avg_ts = avg_ts;
            }
        }

        considered_exp_dates.map(Vec::clone).unwrap_or_else(Vec::new)
    };

    let result = BoosterStates {
        break_: collect("break"),
        drop: collect("drop"),
        fly: collect("fly"),
        mob: collect("mob"),
        xp: collect("xp"),
    };

    return Some(Json(Response { known: any_known, state: result }));
}

#[post("/<citybuild>", format = "json", data = "<state>")]
pub fn post_booster(user: User, data: &State<BoosterData>, constants: &State<Constants>, citybuild: String, state: Json<BoosterStates>) -> Option<Empty> {
    if !constants.valid_citybuilds.contains(&citybuild) {
        // Citybuild is invalid, trigger 400 Bad Request
        return None;
    }

    let mut exp_dates = data.exp_dates.lock().unwrap();
    let citybuild_entries = exp_dates.entry(citybuild)
        .or_insert_with(|| HashMap::new());

    let values = [
        ("break", &state.break_),
        ("drop", &state.drop),
        ("fly", &state.fly),
        ("mob", &state.mob),
        ("xp", &state.xp),
    ];

    // Validate new data
    let now = SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_secs();
    let in_90_min = now + 5400000;
    for (_, exp_dates) in values {
        if exp_dates.iter().any(|date| *date < now && *date > in_90_min) || exp_dates.len() > MAX_LEVELS_PER_TYPE {
            // State is invalid, trigger 400 Bad Request
            return None;
        }
    }

    // Insert new data
    for (booster_type, timestamps) in values {
        let type_entries = citybuild_entries.entry(booster_type.into())
            .or_insert_with(|| HashMap::new());

        type_entries.insert(user.uuid.clone(), (now, timestamps.clone()));
    }

    return Some(Empty {});
}

#[delete("/<citybuild>", data = "<_empty>")]
pub fn delete_booster(user: User, data: &State<BoosterData>, constants: &State<Constants>, citybuild: String, _empty: Empty) -> Option<Empty> {
    if !constants.valid_citybuilds.contains(&citybuild) {
        // Citybuild is invalid, trigger 400 Bad Request
        return None;
    }

    // Remove all data from user
    let mut data = data.exp_dates.lock().unwrap();
    if let Some(citybuild_entries) = data.get_mut(&citybuild) {
        for booster_type in ["break", "drop", "fly", "mob", "xp"] {
            if let Some(type_entries) = citybuild_entries.get_mut(booster_type) {
                type_entries.remove(&user.uuid);
            }
        }
    }

    return Some(Empty {});
}
