use reqwest::Body;
use rocket::http::Status;
use rocket::Request;
use rocket::response::status;
use rocket::serde::json::Json;
use rocket::serde::Serialize;
use rocket::tokio::task::spawn;
use crate::VERSION;

#[catch(default)]
pub fn handle_error(status: Status, _: &Request) -> status::Custom<Json<Error>> {
    // Show Unauthorized, hide everything else as Bad Request
    let res_status = if status == Status::Unauthorized {
        Status::Unauthorized
    } else {
        Status::BadRequest
    };

    status::Custom(
        res_status,
        Json(Error {
            status: res_status.code,
            message: res_status.reason().map(|v| v.into()),
        }),
    )
}

#[derive(Serialize)]
pub struct Error {
    status: u16,
    message: Option<String>,
}

pub fn report_error<T: Into<Body> + Send + 'static>(body: T) {
    spawn(async {
        let client = reqwest::Client::new();

        let _ = client.post("https://grieferutils.l3g7.dev/v4/bug_report")
            .body(body)
            .header("User-Agent", format!("GrieferUtils Server v{}", VERSION))
            .header("Content-Type", "text/plain")
            .send().await;
    });
}

pub fn check_error<T, E: ToString>(res: Result<T, E>) -> Option<T> {
    return match res {
        Err(e) => {
            report_error(e.to_string());
            None
        }
        Ok(b) => Some(b)
    }
}
