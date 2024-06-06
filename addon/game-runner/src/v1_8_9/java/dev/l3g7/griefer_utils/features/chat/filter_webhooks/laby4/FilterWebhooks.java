/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.filter_webhooks.laby4;

import com.google.gson.*;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.api.misc.functions.Runnable;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import net.labymod.api.client.chat.filter.ChatFilter;
import net.labymod.api.configuration.labymod.chat.ChatTab;
import net.labymod.api.configuration.labymod.chat.ChatWindow;
import net.labymod.api.configuration.loader.annotation.SpriteTexture;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.event.client.chat.advanced.AdvancedChatTabMessageEvent;
import net.labymod.core.client.chat.advanced.DefaultAdvancedChatController;
import net.labymod.core.client.chat.filter.DefaultFilterChatService;
import net.labymod.core.main.LabyMod;
import net.minecraft.util.IChatComponent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

@Singleton
@ExclusiveTo(LABY_4)
public class FilterWebhooks extends Feature {

	@MainElement
	private static final SwitchSetting enabled = SwitchSetting.create()
		.name("Webhooks in Filtern")
		.description("Sendet eine Chatnachricht an einen Discord-Webhook, wenn ein LabyMod-Filter auslöst.")
		.icon("webhook");

	private static final Pattern HOOK_URL_PATTERN = Pattern.compile("^https://(?:\\w+\\.)?discord(?:app)?\\.com/api/webhooks/(\\d{18}\\d?/[\\w-]{68})$");
	private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
	private static final JsonObject EMBED_FOOTER = new JsonObject();

	private static final Map<UUID, String> webhooks = new HashMap<>();

	public FilterWebhooks() {
		EMBED_FOOTER.addProperty("text", Constants.ADDON_NAME + " v" + labyBridge.addonVersion());
		EMBED_FOOTER.addProperty("icon_url", "https://grieferutils.l3g7.dev/icon/padded/64x64/");
	}

	private static void saveWebhooks() {
		JsonObject data = new JsonObject();
		for (Map.Entry<UUID, String> entry : webhooks.entrySet())
			data.addProperty(entry.getKey().toString(), entry.getValue());

		Config.set("chat.filter_webhooks.filters.laby4", data);
		Config.save();
	}

	@OnEnable
	private void loadWebhooks() {
		// Convert old webhooks
		if (Config.has("chat.filter_webhooks.filter") && !Config.has("chat.filter_webhooks.filters.laby3")) {
			JsonObject entries = Config.get("chat.filter_webhooks.filter").getAsJsonObject();
			Config.set("chat.filter_webhooks.filters.laby3", entries); // FIXME: test conversion

			DefaultAdvancedChatController advancedChatController = (DefaultAdvancedChatController) LabyMod.references().advancedChatController();
			for (ChatWindow window : advancedChatController.getWindows()) {
				for (ChatTab tab : window.getTabs()) {
					for (ChatFilter filter : tab.config().filters().get()) {
						JsonElement url = entries.get(filter.name().get());
						if (url == null)
							continue;

						webhooks.put(filter.id(), url.getAsString());
					}
				}
			}

			saveWebhooks();
		}

		// Load new webhooks
		if (Config.has("chat.filter_webhooks.filters.laby4"))
			for (Map.Entry<String, JsonElement> entry : Config.get("chat.filter_webhooks.filters.laby4").getAsJsonObject().entrySet())
				webhooks.put(UUID.fromString(entry.getKey()), entry.getValue().isJsonNull() ? null : entry.getValue().getAsString());
	}

	public static void injectSettings(ChatFilter filter, @Nullable Setting parent, CallbackInfoReturnable<List<Setting>> cir) {
		if (!enabled.get())
			return;

		List<Setting> settings = cir.getReturnValue();
		String url = webhooks.get(filter.id());

		// Create settings
		StringSetting urlInput = StringSetting.create()
			.name("Webhook-URL")
			.placeholder("https://discord.com/api/webhooks/...")
			.validator(v -> HOOK_URL_PATTERN.matcher(v).matches())
			.defaultValue(url == null ? "" : url)
			.enabled(url != null)
			.extend()
			.callback(v -> {
				webhooks.put(filter.id(), v);
				saveWebhooks();
			});

		SwitchSetting shouldSend = SwitchSetting.create()
			.name("An Discord-Webhook senden")
			.defaultValue(url != null)
			.callback(v -> {
				urlInput.enabled(v);
				webhooks.put(filter.id(), v && !urlInput.get().isEmpty() ? urlInput.get() : null);
				saveWebhooks();
			});

		// Bind settings
		shouldSend.create(parent);
		settings.add((Setting) shouldSend);

		urlInput.create(parent);
		settings.add((Setting) urlInput);
	}

	public static void triggerWebhook(IChatComponent component, ChatFilter filter) {
		if (!enabled.get())
			return;

		String url = webhooks.get(filter.id());
		if (url == null)
			return;

		// Build payload
		JsonObject root = new JsonObject();
		root.add("content", JsonNull.INSTANCE); // NOTE: replace with GSON?

		JsonArray embeds = new JsonArray();
		JsonObject embed = new JsonObject();
		embed.add("title", sanitize(filter.name().get()));
		embed.add("description", sanitize(component.getUnformattedText().replaceAll("§.", "")));
		embed.add("footer", EMBED_FOOTER);
		if (filter.shouldChangeBackground().get())
			embed.addProperty("color", filter.backgroundColor().get() & 0xFFFFFF);
		embeds.add(embed);
		root.add("embeds", embeds);

		// Send to webhook
		EXECUTOR_SERVICE.execute((Runnable) () -> {
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
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

	@ExclusiveTo(LABY_4)
	@Mixin(net.labymod.api.configuration.loader.Config.class)
	public static class ConfigMixin {

		@Inject(method = "toSettings(Lnet/labymod/api/configuration/settings/Setting;Lnet/labymod/api/configuration/loader/annotation/SpriteTexture;)Ljava/util/List;", at = @At("RETURN"), remap = false)
		public void onToSettings(@Nullable Setting parent, SpriteTexture texture, CallbackInfoReturnable<List<Setting>> cir) {
			if (c(this) instanceof ChatFilter filter)
				injectSettings(filter, parent, cir);
		}

	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = DefaultFilterChatService.class, remap = false)
	public static class DefaultFilterChatServiceMixin {

		@Final
		@Shadow
		private List<ChatFilter> matchingChatFilters;

		@Inject(method = "applyChatFilter", at = @At("RETURN"), remap = false)
		public void onApplyChatFilter(AdvancedChatTabMessageEvent event, CallbackInfo ci) {
			if (event.isCancelled())
				return;

			for (ChatFilter filter : matchingChatFilters)
				triggerWebhook((IChatComponent) event.component(), filter);
		}

	}

}
