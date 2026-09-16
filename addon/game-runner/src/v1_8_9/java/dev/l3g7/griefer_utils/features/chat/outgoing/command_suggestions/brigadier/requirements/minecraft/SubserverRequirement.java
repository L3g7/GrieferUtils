/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.minecraft;

import dev.l3g7.griefer_utils.core.misc.griefer_games.Citybuild;
import dev.l3g7.griefer_utils.features.chat.outgoing.command_suggestions.brigadier.requirements.Requirement;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

/**
 * A requirement for the player to be on fixed GrieferGames subserver.
 */
public class SubserverRequirement extends Requirement {

	private final String server;

	public SubserverRequirement(String server) {
		this.server = server;
	}

	@Override
	public boolean test() {
		if (world() == null)
			// Fall back to true
			return true;

		return Citybuild.parse(server).isOnCb();
	}

}
