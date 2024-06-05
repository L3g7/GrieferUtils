/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.filter_webhooks.laby3;

import com.google.gson.*;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.GuiOpenEvent;
import net.labymod.core_implementation.mc18.gui.GuiChatAdapter;
import net.labymod.ingamechat.tabs.GuiChatFilter;
import net.labymod.ingamechat.tools.filter.Filters;
import net.labymod.main.LabyMod;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;

@Singleton
@ExclusiveTo(LABY_3)
public class FilterWebhooks extends Feature {

    private final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private final JsonObject EMBED_FOOTER = new JsonObject();

    static final Map<String, String> webhooks = new HashMap<>();

	@MainElement
    private final SwitchSetting enabled = SwitchSetting.create()
		.name("Webhooks in Filtern")
		.description("Sendet eine Chatnachricht an einen Discord-Webhook, wenn ein LabyMod-Filter auslöst.")
		.icon("webhook");

    public FilterWebhooks() {
	    EMBED_FOOTER.addProperty("text", Constants.ADDON_NAME + " v" + LabyBridge.labyBridge.addonVersion());
	    EMBED_FOOTER.addProperty("icon_url", "https://grieferutils.l3g7.dev/icon/padded/64x64/");
    }

    static void saveWebhooks() {
        JsonObject data = new JsonObject();
        for (Map.Entry<String, String> entry : webhooks.entrySet())
            data.addProperty(entry.getKey(), entry.getValue());
        Config.set("chat.filter_webhooks.filter", data);
        Config.save();
    }

    @OnEnable
    private void loadWebhooks() {
        if (Config.has("chat.filter_webhooks.filter"))
            for (Map.Entry<String, JsonElement> entry : Config.get("chat.filter_webhooks.filter").getAsJsonObject().entrySet()) {
				String value = entry.getValue().getAsString();
				if (!value.trim().isEmpty())
		            webhooks.put(entry.getKey(), entry.getValue().getAsString());
            }
    }

    @EventListener
    public void onGuiOpen(GuiOpenEvent<GuiScreen> event) {
        if (event.gui instanceof GuiChatFilter)
            event.gui = new CustomGuiChatFilter(Reflection.get(event.gui, "defaultInputFieldText"));
    }

	public static void onMessageReceiveTemp(IChatComponent component) {

	}

    private void onMessageReceive(IChatComponent component) {
        // Check if filters match
        String msg = component.getUnformattedText().toLowerCase().replaceAll("§.", "");
        for (Filters.Filter filter : LabyMod.getInstance().getChatToolManager().getFilters()) {
            if (webhooks.containsKey(filter.getFilterName())
	                && !webhooks.get(filter.getFilterName()).trim().isEmpty()
                    && Arrays.stream(filter.getWordsContains()).anyMatch(w -> msg.contains(w.toLowerCase()))
                    && Arrays.stream(filter.getWordsContainsNot()).noneMatch(w -> msg.contains(w.toLowerCase()))) {
                // Build payload
	            JsonObject root = new JsonObject();
				root.add("content", JsonNull.INSTANCE);

	            JsonArray embeds = new JsonArray();
				JsonObject embed = new JsonObject();
				embed.add("title", sanitize(filter.getFilterName()));
				embed.add("description", sanitize(component.getUnformattedText().replaceAll("§.", "")));
				embed.add("footer", EMBED_FOOTER);
				if (filter.isHighlightMessage())
					embed.addProperty("color", ((filter.getHighlightColorR() & 0xff) << 16) | ((filter.getHighlightColorG() & 0xff) << 8) | (filter.getHighlightColorB() & 0xff));
	            embeds.add(embed);
				root.add("embeds", embeds);

                // Send to webhook
                EXECUTOR_SERVICE.execute(() -> {
	                try {
		                HttpURLConnection conn = (HttpURLConnection) new URL(webhooks.get(filter.getFilterName()).trim()).openConnection();
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
	                } catch (Throwable e) {
		                e.printStackTrace();
	                }
                });
            }
        }
    }


	@ExclusiveTo(LABY_3)
	@Mixin(value = GuiChatAdapter.class, remap = false)
	private static class MixinGuiChatAdapter {

		@Inject(method = "setChatLine", at = @At(value = "INVOKE", target = "Lnet/labymod/ingamechat/renderer/ChatRenderer;getVisualWidth()I"))
		public void postChatLineInitEvent(IChatComponent component, int chatLineId, int updateCounter, boolean refresh, boolean secondChat, String room, Integer highlightColor, CallbackInfo ci) {
			if (!refresh)
				FilterWebhooks.onMessageReceiveTemp(component);
		}

	}


	private JsonElement sanitize(String value) {

		if (value == null)
			return JsonNull.INSTANCE;

		value = value.replaceAll("([^a-zA-Z\\d ])", "\\\\$1");
		return new JsonPrimitive(value);

	}

}