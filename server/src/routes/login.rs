use std::time::SystemTime;

use asn1::{BitString, ObjectIdentifier};
use base64::Engine;
use base64::engine::general_purpose::STANDARD as base64;
use ring::signature::{RSA_PKCS1_2048_8192_SHA1_FOR_LEGACY_USE_ONLY, RSA_PKCS1_2048_8192_SHA256, UnparsedPublicKey};
use rocket::{self, State};
use rocket::serde::{Deserialize, Serialize};
use rocket::serde::json::Json;
use rocket::time::ext::{NumericalDuration, NumericalStdDuration};

use crate::{auth, Constants};
use crate::routes::online_users::OnlineUsersData;

const YGGDRASIL_PUB_KEY: &str = "MIICCgKCAgEAylB4B6m5lz7jwrcFz6Fd/fnfUhcvlxsTSn5kIK/2aGG1C3kMy4Vj\
hwlxF6BFUSnfxhNswPjh3ZitkBxEAFY25uzkJFRwHwVA9mdwjashXILtR6OqdLXX\
FVyUPIURLOSWqGNBtb08EN5fMnG8iFLgEJIBMxs9BvF3s3/FhuHyPKiVTZmXY0WY\
4ZyYqvoKR+XjaTRPPvBsDa4WI2u1zxXMeHlodT3lnCzVvyOYBLXL6CJgByuOxccJ\
8hnXfF9yY4F0aeL080Jz/3+EBNG8RO4ByhtBf4Ny8NQ6stWsjfeUIvH7bU/4zCYc\
YOq4WrInXHqS8qruDmIl7P5XXGcabuzQstPf/h2CRAUpP/PlHXcMlvewjmGU6MfD\
K+lifScNYwjPxRo4nKTGFZf/0aqHCh/EAsQyLKrOIYRE0lDG3bzBh8ogIMLAugsA\
fBb6M3mqCqKaTMAf/VAjh5FFJnjS+7bE+bZEV0qwax1CEoPPJL1fIQjOS8zj086g\
jpGRCtSy9+bTPTfTR/SJ+VUB5G2IeCItkNHpJX2ygojFZ9n5Fnj7R9ZnOM+L8nyI\
jPu3aePvtcrXlyLhH/hvOfIOjPxOlqW+O5QwSFP4OEcyLAUgDdUgyW36Z5mB285u\
KW/ighzZsOTevVUG2QwDItObIV6i8RCxFbN2oDHyPaO5j1tTaBNyVt8CAwEAAQ==";

#[post("/login", format = "json", data = "<request>")]
pub fn login(constants: &State<Constants>, online_users: &State<OnlineUsersData>, request: Json<Request>) -> Option<Json<Response>> {
    // Check if request_time is reasonable
    let request_time = SystemTime::UNIX_EPOCH + request.request_time.std_milliseconds();
    let delta = SystemTime::now().duration_since(request_time).ok()?;
    if delta > 30.seconds() {
        return None;
    }

    // Extract user
    let raw_uuid = hex::decode(&request.user.replace("-", "")).ok()?;

    // Validate signature
    let mut payload = raw_uuid.clone();
    payload.extend_from_slice(request.request_time.to_be_bytes().as_ref());

    let signature = base64.decode(&request.signature).ok()?;

    let raw_key = base64.decode(&request.public_key).ok()?;
    let dec_key = asn1::parse_single::<SubjectPublicKeyInfo>(&raw_key).ok()?;
    let pub_key = UnparsedPublicKey::new(&RSA_PKCS1_2048_8192_SHA256, dec_key.subject_public_key.as_bytes());
    pub_key.verify(payload.as_slice(), &signature).ok()?;

    // Validate public key
    let mut payload = raw_uuid.clone();
    payload.extend_from_slice(request.expiration_time.to_be_bytes().as_ref());
    payload.extend(raw_key);

    let signature = base64.decode(&request.key_signature).ok()?;

    let raw_key = base64.decode(YGGDRASIL_PUB_KEY).unwrap();
    let pub_key = UnparsedPublicKey::new(&RSA_PKCS1_2048_8192_SHA1_FOR_LEGACY_USE_ONLY, &raw_key);
    pub_key.verify(payload.as_slice(), &signature).ok()?;

    // Generate JWT
    let user = auth::User { uuid: request.user.clone() };
    let jwt = user.generate_jwt(constants);
    online_users.ping(user.uuid);

    jwt.map(|t| Json(Response { session_token: t }))
}

#[derive(Deserialize)]
#[serde(deny_unknown_fields)]
pub struct Request {
    user: String,
    request_time: u64,
    signature: String,
    public_key: String,
    key_signature: String,
    expiration_time: u64,
}

#[derive(Serialize)]
pub struct Response {
    session_token: String,
}

#[allow(dead_code)]
#[derive(asn1::Asn1Read)]
struct SubjectPublicKeyInfo<'a> {
    algorithm: AlgorithmIdentifier,
    subject_public_key: BitString<'a>,
}

#[allow(dead_code)]
#[derive(asn1::Asn1Read)]
struct AlgorithmIdentifier {
    algorithm: ObjectIdentifier,
    parameters: (),
}
