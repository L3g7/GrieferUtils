use std::collections::HashMap;
use std::sync::Mutex;
use std::time::{Duration, SystemTime};

use rocket::serde::{Deserialize, Serialize};
use rocket::serde::json::Json;
use rocket::State;

use crate::auth::User;

#[derive(Default)]
pub struct OnlineUsersData {
    /// Map from UUID to time of last ping
    online_users: Mutex<HashMap<String, SystemTime>>,
}

impl OnlineUsersData {
    pub fn ping(&self, user: String) {
        let mut online_users = self.online_users.lock().unwrap();
        online_users.insert(user.into(), SystemTime::now());
    }

    pub fn has(&self, user: &str) -> bool {
        let mut online_users = self.online_users.lock().unwrap();
        if let Some(last_ping) = online_users.get(user) {
            // Check if last ping is less than 30s ago
            let min_ts = SystemTime::now() - Duration::from_secs(30);
            if &min_ts > last_ping {
                online_users.remove(user);
                return false;
            }

            return true;
        }

        false
    }

    pub fn remove(&self, user: &str) {
        self.online_users.lock().unwrap().remove(user);
    }
}

#[derive(Deserialize)]
#[serde(deny_unknown_fields)]
pub struct Request {
    users_requested: Vec<String>,
}

#[derive(Serialize)]
pub struct Response {
    users_online: Vec<String>,
}

#[post("/online_users", format = "json", data = "<request>")]
pub fn online_users(_user: User, online_users: &State<OnlineUsersData>, request: Json<Request>) -> Json<Response> {
    let mut users_online = Vec::new();

    for user in &request.users_requested {
        if online_users.has(&user) {
            users_online.push(user.clone());
        }
    }

    Json(Response { users_online })
}
