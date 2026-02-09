/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.render;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.network.LabyModNeoPayloadEvent;
import dev.l3g7.griefer_utils.core.events.network.LabyModNeoPayloadEvent.SubtitlePacket;
import dev.l3g7.griefer_utils.core.events.network.LabyModNeoPayloadEvent.SubtitlePacket.Subtitle;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.misc.tags.Tags.TagManager.tagManager;

/**
 * Shows a player's clan tag underneath their name tag.
 */
@Singleton
@ExclusiveTo(LABY_3)
public class ClanTags extends Feature {

	@MainElement
	private static final SwitchSetting enabled = SwitchSetting.create()
		.name("Clantags")
		.description("Zeigt den Clantag eines Spielers unter seinem Nametag.")
		.icon("rainbow_name")
		.callback(tagManager::toggleSubtitles);

	@EventListener(triggerWhenDisabled = true)
	public void onSubtitle(LabyModNeoPayloadEvent<SubtitlePacket> event) {
		for (Subtitle subtitle : event.packet.subtitles)
			tagManager.setSubtitle(subtitle.uuid(), subtitle.text(), subtitle.scale());
	}

	public static boolean showSubtitle() {
		return enabled.get();
	}

}
