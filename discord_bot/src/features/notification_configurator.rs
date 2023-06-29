use std::env;
use std::str::FromStr;
use std::sync::Arc;
use std::time::Duration;

use phf::{Map, phf_map};
use serenity::client::Context;
use serenity::collector::ComponentInteractionCollectorBuilder;
use serenity::futures::StreamExt;
use serenity::model::application::component::ButtonStyle;
use serenity::model::application::interaction::InteractionResponseType;
use serenity::model::application::interaction::message_component::MessageComponentInteraction;
use serenity::model::prelude::{ChannelId, EmojiId, Member, MessageId, ReactionType, RoleId};

static NOTIFICATION_ROLES: Map<&'static str, NotificationRole> = phf_map! {
    "1123611066778275840" => NotificationRole {
		title: "Bei Ankündigungen",
		description: Some("Ankündigungen sind wichtige Nachrichten, Umfragen o.Ä."),
		emoji: 1121182678398541917u64
	},
    "1123611104510222408" => NotificationRole {
		title: "Bei neuen Versionen",
		description: Some("Diese Versionen enthalten wenig Bugs."),
		emoji: 1121159814383419524u64
	},
    "1123611140652535878" => NotificationRole {
		title: "Bei neuen Testversionen",
		description: Some("Diese Versionen enthalten unveröffentlichte Features."),
		emoji: 1121159814383419524u64
	},
    "1123611182507491359" => NotificationRole {
		title: "Bei Codeänderungen (Commits)",
		description: None,
		emoji: 1121159812311433276u64
	},
};

struct NotificationRole {
	title: &'static str,
	description: Option<&'static str>,
	emoji: u64,
}

async fn get_open_message_id(ctx: &Context) -> Result<MessageId, serenity::Error> {
	let token = env::var("NOTIFICATION_CONFIGURATOR_OPEN_MESSAGE_ID");
	if let Ok(token) = token {
		return Ok(MessageId::from(token.parse::<u64>().expect("Could not parse NOTIFICATION_CONFIGURATOR_OPEN_MESSAGE_ID")));
	}

	Ok(ChannelId::from(1123572206094532741u64)
		.send_message(ctx, |m| {
			m.content("# Wähle aus, wann du Benachrichtigungen erhalten möchtest.").components(|c| {
				c.create_action_row(|row| {
					row.create_button(|button| {
						button.label("Benachrichtigungen auswählen")
							.emoji(ReactionType::from(EmojiId(1123624499376562257u64)))
							.custom_id("open_notification_configurator")
							.style(ButtonStyle::Secondary)
					})
				})
			})
		})
		.await?.id)
}

async fn send_configurator_message(ctx: Context, interaction: Arc<MessageComponentInteraction>, member: Member) -> Result<(), serenity::Error> {
	// Send dropdown
	interaction
		.create_interaction_response(&ctx, |res| {
			res.kind(InteractionResponseType::ChannelMessageWithSource)
				.interaction_response_data(|r| {
					r.ephemeral(true)
						.components(|c| {
						c.create_action_row(|row| {
							row.create_select_menu(|menu| {
								menu.custom_id("notify_select")
									.placeholder("Du erhältst keine Benachrichtungen.")
									.min_values(0)
									.max_values(NOTIFICATION_ROLES.len() as u64)
									.options(|options| {
										for (id, role) in &NOTIFICATION_ROLES {
											options.create_option(|option| {
												option.label(role.title)
													.value(id)
													.emoji(ReactionType::from(EmojiId(role.emoji)));

												// Set selected if member has the role
												if member.roles.contains(&RoleId::from_str(id).unwrap()) {
													option.default_selection(true);
												}

												// Set description
												if let Some(description) = &role.description {
													option.description(description);
												};

												option
											});
										}

										options
									})
							})
						})
					})
				})
		}).await.expect("Could not respond to interaction");

	let message = interaction.get_interaction_response(&ctx).await;

	// React to dropdown selection
	if let Ok(message) = message {
		let mut stream = message.await_component_interactions(&ctx)
			.timeout(Duration::from_secs(3600 * 24 * 2))
			.build();
		if let Some(role_selection) = stream.next().await {
			// Acknowledge interaction
			role_selection
				.create_interaction_response(&ctx, |res| {
					res.kind(InteractionResponseType::DeferredUpdateMessage)
						.interaction_response_data(|data| {
							data.ephemeral(true)
						})
				})
				.await?;

			// Process selection
			let values = &role_selection.data.values;

			for key in NOTIFICATION_ROLES.keys() {
				let role_id = RoleId::from_str(key).unwrap();
				if member.roles.contains(&role_id) {
					// Member has role, but not in selected -> remove
					if !values.contains(&key.to_string()) {
						let _ = &ctx.http.as_ref().remove_member_role(member.guild_id.0, member.user.id.0, role_id.0, None).await?;
					}
				} else {
					// Member does not have the role, but in selected -> add
					if values.contains(&key.to_string()) {
						let _ = &ctx.http.as_ref().add_member_role(member.guild_id.0, member.user.id.0, role_id.0, None).await?;
					}
				}
			}

			// Delete dropdown
			role_selection
				.delete_original_interaction_response(&ctx)
				.await?;

		}
	};
	Ok(())
}

pub async fn start(ctx: &Context) {
	let open_message_id = get_open_message_id(ctx).await.unwrap();

	let mut open_button_stream = ComponentInteractionCollectorBuilder::new(ctx).message_id(open_message_id).build();
	while let Some(interaction) = open_button_stream.next().await {
		let button = &interaction.data.custom_id;
		if let Some(member) = &interaction.member {
			if button.eq("open_notification_configurator") {
				// TODO: find better way to pass parameters into task
				let ctx_clone = ctx.clone();
				let interaction_clone = interaction.clone();
				let member_clone = member.clone();
				tokio::task::spawn(async move { send_configurator_message(ctx_clone, interaction_clone, member_clone).await });
			}
		}
	}
}
