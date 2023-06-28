use std::env;

use dotenv::dotenv;
use serenity::{async_trait, Client};
use serenity::client::{Context, EventHandler};
use serenity::model::prelude::Ready;
use serenity::prelude::GatewayIntents;

use	features::notification_configurator;

mod features;

struct Handler;

#[async_trait]
impl EventHandler for Handler {

	async fn ready(&self, ctx: Context, _data_about_bot: Ready) {
		notification_configurator::start(&ctx).await;
	}

}

#[tokio::main]
async fn main() {
	dotenv().ok();
	let token = env::var("DISCORD_TOKEN").expect("Could not find discord token.");

	let mut client = Client::builder(token, GatewayIntents::empty())
		.event_handler(Handler)
		.await.unwrap();

	client.start().await.ok();
}