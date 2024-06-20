/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.render.clan_tags.laby4;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.network.MysteryModPayloadEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.labymod.core.main.LabyMod;
import net.labymod.serverapi.api.model.component.ServerAPIComponent;
import net.labymod.serverapi.core.model.display.Subtitle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

/**
 * Shows a player's clan tag underneath their name tag.
 */
@Singleton
@ExclusiveTo(LABY_4)
public class ClanTags extends Feature {

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
	public void onMMPayload(MysteryModPayloadEvent event) {
		if (!event.channel.equals("user_subtitle"))
			return;

		for (JsonElement elem : event.payload.getAsJsonArray()) {
			JsonObject obj = elem.getAsJsonObject();

			UUID uuid = UUID.fromString(obj.get("targetId").getAsString());
			Subtitle subtitle = Subtitle.create(uuid, ServerAPIComponent.text(obj.get("text").getAsString()), 0.8);

			subtitles.add(subtitle);
			if (isEnabled())
				mc().addScheduledTask(() -> LabyMod.references().subtitleService().addSubtitle(subtitle));
		}
	}

}
