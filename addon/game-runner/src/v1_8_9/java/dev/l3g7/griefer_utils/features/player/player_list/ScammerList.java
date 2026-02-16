/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.player_list;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;

import static net.minecraft.util.EnumChatFormatting.RED;

@Singleton
public class ScammerList extends PlayerList {

	public ScammerList() {
		super("§xScammerliste", "Markiert Spieler in der Scammerliste des CommunityRadars.", "⚠", "player_red", "Eigene Scammer", RED, 14, "§c§lScammer", "https://data.community-radar.de/versions/v2/scammer.json");
	}

	public static ScammerList get() {
		return get(ScammerList.class);
	}

}
