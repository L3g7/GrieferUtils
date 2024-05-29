/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.network.MysteryModPayloadEvent;
import net.labymod.core.main.LabyMod;
import net.labymod.serverapi.protocol.model.display.Subtitle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_4;

/**
 * Shows a player's clan tag underneath their name tag.
 */
@Singleton
@ExclusiveTo(LABY_4)
public class ClanTagsL4 extends Feature {

	private final List<Subtitle> subtitles = new ArrayList<>();

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Clantags")
		.description("Zeigt den Clantag eines Spielers unter seinem Nametag.")
		.icon("rainbow_name")
		.callback(this::toggleClanTags);

	private void toggleClanTags(boolean enabled) {
		for (Subtitle subtitle : subtitles) {
			if (enabled)
				LabyMod.references().subtitleService().addSubtitle(subtitle);
			else
				LabyMod.references().subtitleService().removeSubtitle(subtitle);
		}
	}

	@EventListener(triggerWhenDisabled = true)
	public void onPlayerTick(MysteryModPayloadEvent event) {
		if (!event.channel.equals("user_subtitle"))
			return;

		for (JsonElement elem : event.payload.getAsJsonArray()) {
			JsonObject obj = elem.getAsJsonObject();

			UUID uuid = UUID.fromString(obj.get("targetId").getAsString());
			Subtitle subtitle = new Subtitle(uuid, 0.8, obj.get("text"));

			subtitles.add(subtitle);
			if (isEnabled())
				LabyMod.references().subtitleService().addSubtitle(subtitle);
		}
	}

}
