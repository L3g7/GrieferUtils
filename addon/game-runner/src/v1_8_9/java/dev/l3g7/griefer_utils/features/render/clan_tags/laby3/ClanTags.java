/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.render.clan_tags.laby3;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.network.MysteryModPayloadEvent;
import net.labymod.main.LabyMod;
import net.labymod.user.User;
import net.labymod.utils.ModColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;

/**
 * Shows a player's clan tag underneath their name tag.
 */
@Singleton
@ExclusiveTo(LABY_3)
public class ClanTags extends Feature {

	private final Map<UUID, String> tags = new HashMap<>();

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Clantags")
		.description("Zeigt den Clantag eines Spielers unter seinem Nametag.")
		.icon("rainbow_name")
		.callback(this::toggleClanTags);

	private void toggleClanTags(boolean enabled) {
		if (enabled) {
			tags.forEach((uuid, tag) -> {
				User user = LabyMod.getInstance().getUserManager().getUser(uuid);
				user.setSubTitle(tag);
				user.setSubTitleSize(0.8);
			});
		} else {
			for (User user : LabyMod.getInstance().getUserManager().getUsers().values())
				user.setSubTitle(null);
		}
	}

	@EventListener(triggerWhenDisabled = true)
	public void onPlayerTick(MysteryModPayloadEvent event) {
		if (!event.channel.equals("user_subtitle"))
			return;

		for (JsonElement elem : event.payload.getAsJsonArray()) {
			JsonObject obj = elem.getAsJsonObject();

			UUID uuid = UUID.fromString(obj.get("targetId").getAsString());
			User user = LabyMod.getInstance().getUserManager().getUser(uuid);

			String tag = ModColor.createColors(obj.get("text").getAsString());
			tags.put(uuid, tag);
			if (isEnabled()) {
				user.setSubTitle(tag);
				user.setSubTitleSize(obj.get("scale").getAsDouble());
			}
		}
	}

}
