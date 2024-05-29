use rocket::State;

use crate::auth::User;
use crate::routes::online_users::OnlineUsersData;
use crate::util::Empty;

#[post("/keep_alive", data = "<_empty>")]
pub fn keep_alive(user: User, online_users: &State<OnlineUsersData>, _empty: Empty) -> Empty {
    online_users.ping(user.uuid);

    Empty {}
}
