use std::time::{SystemTime, UNIX_EPOCH};

use jsonwebtoken::{decode, encode};
use rocket::http::Status;
use rocket::request::{FromRequest, Outcome};
use rocket::serde::{Deserialize, Serialize};
use rocket::State;
use rocket::time::ext::NumericalDuration;

use crate::Constants;

#[derive(Serialize, Deserialize, Debug)]
struct JwtClaims {
    exp: u64,
    iat: u64,
    iss: String,
    sub: String,
}

pub struct User {
    pub uuid: String,
}

impl User {
    pub fn generate_jwt(&self, constants: &State<Constants>) -> Option<String> {
        let claims = JwtClaims {
            exp: (SystemTime::now() + 24.hours()).duration_since(UNIX_EPOCH).ok()?.as_secs(),
            iat: UNIX_EPOCH.elapsed().ok()?.as_secs(),
            iss: "https://s1.grieferutils.l3g7.dev/login".into(),
            sub: self.uuid.clone(),
        };

        encode(&constants.jwt_header, &claims, &constants.jwt_encoding_key).ok()
    }
}

#[rocket::async_trait]
impl<'r> FromRequest<'r> for User {
    type Error = ();

    async fn from_request(request: &'r rocket::Request<'_>) -> Outcome<Self, ()> {
        return if let Some(user) = try_auth(request) {
            Outcome::Success(user)
        } else {
            Outcome::Error((Status::Unauthorized, ()))
        };
    }
}

fn try_auth(request: &rocket::Request) -> Option<User> {
    let auth = request.headers().get_one("Authorization")?;
    if !auth.starts_with("Bearer ") {
        return None;
    }

    let token_str = &auth[7..];
    let constants = request.rocket().state::<Constants>()?;

    let token = decode::<JwtClaims>(&token_str, &constants.jwt_decoding_key, &constants.jwt_validation).ok()?.claims;

    return Some(User {
        uuid: token.sub
    });
}
