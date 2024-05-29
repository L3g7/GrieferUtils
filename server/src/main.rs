#[macro_use]
extern crate rocket;

use std::env;

use dotenv::dotenv;
use jammdb::DB;
use jsonwebtoken::{Algorithm, DecodingKey, EncodingKey, Header, Validation};
use rocket::shield::{Hsts, Shield};
use rocket::time::ext::NumericalDuration;

use error_handler::handle_error;

use crate::routes::hive_mind::booster::{BoosterData, delete_booster, get_booster, post_booster};
use crate::routes::hive_mind::mob_remover::{get_mob_remover, MobRemoverData, post_mob_remover};
use crate::routes::keep_alive::keep_alive;
use crate::routes::leaderboard::{get_leaderboard, post_leaderboard};
use crate::routes::login::login;
use crate::routes::logout::logout;
use crate::routes::online_users::{online_users, OnlineUsersData};

mod error_handler;
mod routes;
mod util;
mod auth;
mod db;

const VERSION: &str = "2.0";

#[launch]
fn rocket() -> _ {
    dotenv().unwrap();

    rocket::build()
        .manage(Constants::new())
        .manage(DB::open("server.db").unwrap())
        .manage(OnlineUsersData::default())
        .manage(BoosterData::default())
        .manage(MobRemoverData::default())
        .mount("/", routes![keep_alive, login, logout, online_users, get_leaderboard, post_leaderboard])
        .mount("/hive_mind/booster", routes![get_booster, post_booster, delete_booster])
        .mount("/hive_mind/mob_remover", routes![get_mob_remover, post_mob_remover])
        // Error handler
        .register("/", catchers![handle_error])
        // Security headers (HSTS)
        .attach(Shield::new().enable(Hsts::Preload(63072000.seconds())))
}

pub struct Constants {
    jwt_header: Header,
    jwt_encoding_key: EncodingKey,
    jwt_decoding_key: DecodingKey,
    jwt_validation: Validation,

    valid_citybuilds: Vec<String>,
    discord_token: String,
}

impl Constants {
    fn new() -> Constants {
        let jwt_secret = env::var("JWT_SECRET").unwrap();
        let algorithm = Algorithm::HS384;
        let citybuilds = env::var("VALID_CITYBUILDS").unwrap();

        Constants {
            jwt_header: Header::new(algorithm),
            jwt_encoding_key: EncodingKey::from_secret(jwt_secret.as_ref()),
            jwt_decoding_key: DecodingKey::from_secret(jwt_secret.as_ref()),
            jwt_validation: Validation::new(algorithm),

            valid_citybuilds: citybuilds.split(",").map(str::to_string).collect(),
            discord_token: env::var("DISCORD_TOKEN").unwrap(),
        }
    }
}
