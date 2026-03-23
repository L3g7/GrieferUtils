/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.name_tags;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.events.network.LabyModNeoPayloadEvent;
import dev.l3g7.griefer_utils.core.events.network.LabyModNeoPayloadEvent.SubtitlePacket;
import dev.l3g7.griefer_utils.core.events.network.LabyModNeoPayloadEvent.SubtitlePacket.Subtitle;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.labymod.main.LabyMod;
import net.labymod.user.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Reason.NOT_NEEDED;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;

/**
 * Shows a player's clan tag underneath their name tag.
 */
@Singleton
@ExclusiveTo(value = LABY_3, reason = NOT_NEEDED, customMessage = "GrieferGames hat native Unterstützung für LabyMod 4 Clan Tags.")
public class ClanTags extends Feature {

	private static final Map<UUID, Pair<String, Double>> subtitles = new HashMap<>();

	@MainElement
	private static final SwitchSetting enabled = SwitchSetting.create()
		.name("Clantags")
		.description("Zeigt den Clantag eines Spielers unter seinem Nametag.")
		.icon("name_tag_rainbow")
		.callback(ClanTags::toggleSubtitles);

	@EventListener(triggerWhenDisabled = true)
	public void onSubtitle(LabyModNeoPayloadEvent<SubtitlePacket> event) {
		for (Subtitle subtitle : event.packet.subtitles)
			setSubtitle(subtitle.uuid(), subtitle.text(), subtitle.scale());
	}

	private static void setSubtitle(UUID uuid, String text, double scale) {
		subtitles.put(uuid, new Pair<>(text, scale));
		if (!enabled.get())
			return;

		User user = LabyMod.getInstance().getUserManager().getUser(uuid);
		user.setSubTitle(text);
		user.setSubTitleSize(scale);
	}

	private static void toggleSubtitles(boolean enabled) {
		if (enabled)
			subtitles.forEach((uuid, tag) -> setSubtitle(uuid, tag.a, tag.b));
		else
			for (User user : LabyMod.getInstance().getUserManager().getUsers().values())
				user.setSubTitle(null);
	}

}
