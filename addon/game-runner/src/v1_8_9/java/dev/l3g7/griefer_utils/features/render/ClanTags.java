/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.network.MysteryModPayloadEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;

import java.util.UUID;

import static dev.l3g7.griefer_utils.core.misc.tags.Tags.TagManager.tagManager;

/**
 * Shows a player's clan tag underneath their name tag.
 */
@Singleton
public class ClanTags extends Feature {

	@MainElement
	private static final SwitchSetting enabled = SwitchSetting.create()
		.name("Clantags")
		.description("Zeigt den Clantag eines Spielers unter seinem Nametag.")
		.icon("rainbow_name")
		.callback(tagManager::toggleSubtitles);

	@EventListener(triggerWhenDisabled = true)
	public void onPlayerTick(MysteryModPayloadEvent event) {
		if (!event.channel.equals("user_subtitle"))
			return;

		for (JsonElement elem : event.payload.getAsJsonArray()) {
			JsonObject obj = elem.getAsJsonObject();

			UUID uuid = UUID.fromString(obj.get("targetId").getAsString());

			String text = obj.get("text").getAsString().replaceAll("(?i)&([a-z0-9])", "§$1");
			double scale = obj.get("scale").getAsDouble();
			tagManager.setSubtitle(uuid, text, scale);
		}
	}

	public static boolean showSubtitle() {
		return enabled.get();
	}

}
