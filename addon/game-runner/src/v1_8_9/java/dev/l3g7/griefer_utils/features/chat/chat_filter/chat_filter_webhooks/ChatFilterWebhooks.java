/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.chat_filter.chat_filter_webhooks;

import com.google.gson.*;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.misc.ThreadFactory;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.api.misc.functions.Runnable;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.util.IChatComponent;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.STATIC_API_URL;
import static java.lang.Thread.MIN_PRIORITY;

@Singleton
public class ChatFilterWebhooks extends Feature {

	public static final Pattern HOOK_URL_PATTERN = Pattern.compile("^https://(?:\\w+\\.)?discord(?:app)?\\.com/api/webhooks/(\\d{18}\\d?/[\\w-]{68})$");
	private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor(new ThreadFactory("GrieferUtils ChatFilter Webhook", MIN_PRIORITY));
	private static final JsonObject EMBED_FOOTER = new JsonObject();

	static {
		EMBED_FOOTER.addProperty("text", Constants.ADDON_NAME + " v" + labyBridge.addonVersion());
		EMBED_FOOTER.addProperty("icon_url", STATIC_API_URL + "/icon/padded/64x64.png");
	}

	public static final Map<String, String> webhooks = new HashMap<>();
	private static String configKey;

	private static final DropDownSetting<Style> messageStyle = DropDownSetting.create(Style.class)
		.name("Nachrichten-Stil")
		.icon("color_palette")
		.defaultValue(Style.EMBED);

	@MainElement
	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("Webhooks in Filtern")
		.description("Sendet eine Chatnachricht an einen Discord-Webhook, wenn ein LabyMod-Filter auslöst.")
		.icon("discord")
		.subSettings(messageStyle);

	public static Map<String, String> getWebhooks() {
		return webhooks;
	}

	@Override
	public void init() {
		super.init();
		configKey = "chat.chat_filter.chat_filter_webhooks.filters." + (LABY_3.isActive() ? "laby3" : "laby4");
		if (!Config.has(configKey))
			return;

		for (Map.Entry<String, JsonElement> entry : Config.get(configKey).getAsJsonObject().entrySet()) {
			String value = entry.getValue().isJsonNull() ? null : entry.getValue().getAsString();
			if (value == null || value.trim().isEmpty())
				continue;

			webhooks.put(entry.getKey(), value);
		}
	}

	public static void saveWebhooks() {
		JsonObject data = new JsonObject();
		for (Map.Entry<String, String> entry : webhooks.entrySet())
			data.addProperty(entry.getKey(), entry.getValue());

		Config.set(configKey, data);
		Config.save();
	}

	public static void triggerWebhook(String url, IChatComponent component, String name, Integer color) {
		if (!enabled.get())
			return;

		if (url == null)
			return;

		// Build payload
		JsonObject root = new JsonObject();
		if (messageStyle.get() == Style.EMBED) {
			root.add("content", JsonNull.INSTANCE);

			JsonArray embeds = new JsonArray();
			JsonObject embed = new JsonObject();
			embed.add("title", sanitize(name));
			embed.add("description", sanitize(component.getUnformattedText().replaceAll("§.", "")));
			embed.add("footer", EMBED_FOOTER);
			if (color != null)
				embed.addProperty("color", color & 0xFFFFFF);

			embeds.add(embed);
			root.add("embeds", embeds);
		} else
			root.add("content", sanitize(component.getUnformattedText().replaceAll("§.", "")));

		// Send to webhook
		EXECUTOR_SERVICE.execute((Runnable) () -> {
			HttpURLConnection conn = (HttpURLConnection) new URI(url.trim()).toURL().openConnection();
			conn.setConnectTimeout(3000);
			conn.setReadTimeout(10000);
			conn.addRequestProperty("User-Agent", "GrieferUtils");
			conn.addRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);

			conn.setRequestMethod("POST");

			try (OutputStream stream = conn.getOutputStream()) {
				stream.write(root.toString().getBytes(StandardCharsets.UTF_8));
				stream.flush();
			}

			conn.getInputStream().close();
		});
	}

	private static JsonElement sanitize(String value) {
		if (value == null)
			return JsonNull.INSTANCE;

		value = value.replaceAll("([^a-zA-Z\\d ])", "\\\\$1");
		return new JsonPrimitive(value);
	}

	private enum Style implements Named {
		EMBED("Embed"),
		TEXT("Text");

		private final String name;

		Style(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
	}
}
