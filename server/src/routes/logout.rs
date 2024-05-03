use rocket::State;

use crate::auth::User;
use crate::routes::online_users::OnlineUsersData;
use crate::util::Empty;

#[post("/logout", data = "<_empty>")]
pub fn logout(user: User, online_users: &State<OnlineUsersData>, _empty: Empty) -> Empty {
    online_users.remove(&user.uuid);

    Empty {}
}
