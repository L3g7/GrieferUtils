/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.config.patches;

import dev.l3g7.griefer_utils.core.api.misc.config.ConfigPatcher.ConfigPatch;

public class To2_0_RC_12 extends ConfigPatch {

	@Override
	public boolean shouldApply() {
		return isConfigOlderThan("2.0-RC-12");
	}

	@Override
	public void patch() {
		// Merged ShowSpawnerIcons, SpawnerWithHeldItemFix into BetterSpawners
		rename("world.show_spawner_icons", "world.better_spawners.show_spawner_icons");
		var betterSpawners = get("world.better_spawners");

		var spawnerWithHeldItemFix = get("world.spawner_with_held_item_fix");
		if (spawnerWithHeldItemFix.isTrue("enabled")) {
			betterSpawners.set("enabled", true);
			betterSpawners.set("spawner_with_held_item_fix", true);
		}

		var showSpawnerIcons = get("world.show_spawner_icons");
		if (showSpawnerIcons.isTrue("enabled"))
			betterSpawners.set("enabled", true);
	}

}
