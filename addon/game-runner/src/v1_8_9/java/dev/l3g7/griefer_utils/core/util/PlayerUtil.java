/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ScorePlayerTeam;

import java.util.UUID;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

/**
 * A utility class for minecraft player related methods.
 */
public class PlayerUtil {

	/**
	 * @return the player's uuid.
	 */
	public static UUID getUUID(String name) {
		if (mc().getNetHandler() == null)
			return null;

		for(NetworkPlayerInfo info : mc().getNetHandler().getPlayerInfoMap()) {
			if(info.getGameProfile().getName().equalsIgnoreCase(name))
				return info.getGameProfile().getId();

			if(info.getDisplayName() != null) {
				String[] parts = info.getDisplayName().getUnformattedText().split("\u2503");
				if(parts.length > 1 && parts[1].trim().equalsIgnoreCase(name))
					return info.getGameProfile().getId();
			}
		}
		return null;
	}

	public static boolean isValid(String name) {
		return name != null && (name.matches("^\\w{3,}$") || name.startsWith("!"));
	}

	public static String getRank(String name) {
		NetworkPlayerInfo info = mc().getNetHandler().getPlayerInfo(name);
		if (info == null)
			return "";

		ScorePlayerTeam team = info.getPlayerTeam();
		if (team == null)
			return "";

		return team.getColorPrefix().replaceAll("§.", "").split("┃")[0];
	}

	public static String getName() {
		return mc().getSession().getUsername();
	}

	public static boolean isNPC(Entity entity) {
		return entity instanceof EntityOtherPlayerMP && entity.getCustomNameTag().isEmpty();
	}

}
