const nacl = require("tweetnacl")

const DISCORD_API = "https://discord.com/api/v10/"

const NOTIFICATION_ROLES = [
	{
		title: "Bei Ankündigungen",
		description: "Ankündigungen sind wichtige Nachrichten, Umfragen o.Ä.",
		id: "1123611066778275840",
		emoji: "1121182678398541917"
	},
	{
		title: "Bei neuen Versionen",
		description: "Diese Versionen enthalten wenig Bugs.",
		id: "1123611104510222408",
		emoji: "1121159814383419524"
	},
	{
		title: "Bei neuen Testversionen",
		description: "Diese Versionen enthalten unveröffentlichte Features.",
		id: "1123611140652535878",
		emoji: "1121159814383419524"
	},
	{
		title: "Bei Codeänderungen (Commits)",
		id: "1123611182507491359",
		emoji: "1121159812311433276"
	}
]

export default {
	async fetch(request, env, ctx) {
		// Early checks
		if (new URL(request.url).pathname !== '/' || request.method !== 'POST'
			|| !request.headers.get("X-Signature-Timestamp") || !request.headers.get("X-Signature-Ed25519")) {
			return Response.json({ status: 400, message: "Bad Request" }, { status: 400 })
		}

		// Grab body
		const rawBody = await request.text()
		var body
		try {
			body = JSON.parse(rawBody)
		} catch (e) {
			return Response.json({ status: 400, message: "Bad Request" }, { status: 400 })
		}

		// Verify message
		var isVerified
		try {
			const fromHexString = (hexString) => Uint8Array.from(hexString.match(/.{1,2}/g).map((byte) => parseInt(byte, 16)))

			isVerified = nacl.sign.detached.verify(
				new TextEncoder().encode(request.headers.get("X-Signature-Timestamp") + rawBody),
				fromHexString(request.headers.get("X-Signature-Ed25519")),
				fromHexString(env.DISCORD_PUBKEY)
			)
		} catch (e) {
			isVerified = false
		}

		if (!isVerified) {
			return Response.json({ status: 401, message: "Unauthorized" }, { status: 401 })
		}

		if (body.type === 1 /* PING */) {
			return Response.json({ type: 1 /* PONG */ }, { status: 200 })
		}

		if (body.type === 3 /* MESSAGE_COMPONENT */) {
			switch (body.data.custom_id) {
				case "open_notification_configurator":
					return Response.json(createSelectionMessage(body.member.roles, false), { status: 200 })

				case "notify_select":
					ctx.waitUntil(new Promise(async (resolve) => {
						// Remove selection message
						await fetch(`${DISCORD_API}/webhooks/${body.application_id}/${body.token}/messages/@original`, {
							method: "DELETE",
						})

						// Update roles
						for (let role of NOTIFICATION_ROLES) {
							if (body.member.roles.includes(role.id) != body.data.values.includes(role.id)) {
								let method = body.data.values.includes(role.id) ? 'PUT' : 'DELETE'
								await fetch(`${DISCORD_API}/guilds/${body.guild_id}/members/${body.member.user.id}/roles/${role.id}`, {
									method,
									headers: {
										Authorization: `Bot ${env.DISCORD_TOKEN}`,
										"X-Audit-Log-Reason": "Notification selection"
									}
								})
							}
						}

						resolve()
					}))
					return Response.json(createSelectionMessage(body.data.values, true), { status: 200 })
			}
		}

		return Response.json({ status: 400, message: "Bad Request" }, { status: 400 })
	},
}

function createSelectionMessage(selectedRoles, disabled) {
	return {
		type: disabled ? 7 /* UPDATE_MESSAGE */ : 4 /* CHANNEL_MESSAGE_WITH_SOURCE */,
		data: {
			flags: (1 << 6) /* EPHEMERAL */ | (1 << 12) /* SUPPRESS_NOTIFICATIONS */,
			components: [
				{
					type: 1 /* Action Row */,
					components: [
						{
							type: 3 /* String Select */,
							custom_id: "notify_select",
							options: NOTIFICATION_ROLES.map(role => ({
								label: role.title,
								description: role.description,
								value: role.id,
								emoji: {
									id: role.emoji
								},
								default: selectedRoles.includes(role.id)
							})),
							placeholder: "Du erhältst keine Benachrichtungen.",
							min_values: 0,
							max_values: NOTIFICATION_ROLES.length,
							disabled: disabled
						}
					]
				}
			]
		}
	}
}
