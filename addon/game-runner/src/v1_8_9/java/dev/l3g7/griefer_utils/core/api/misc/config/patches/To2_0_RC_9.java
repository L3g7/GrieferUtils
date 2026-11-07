/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.config.patches;

import dev.l3g7.griefer_utils.core.api.misc.config.ConfigPatcher.ConfigPatch;

public class To2_0_RC_9 extends ConfigPatch {

	@Override
	public boolean shouldApply() {
		return isConfigOlderThan("2.0-RC-9");
	}

	@Override
	public void patch() {
		// Renamed BlockPreview -> BlockInfo
		rename("modules.block_preview.show_coordinates", "modules.block_info.show_coords");

		// Renamed TPS -> ServerPerformance
		rename("modules", "tps", "server_performance");

		// Renamed OrbPotionTimer -> PotionTimer
		rename("modules", "orb_potion_timer", "potion_timer");

		// Normalized config
		rename("modules.spawn_counter", "roundsRan", "rounds_ran");
		rename("modules.spawn_counter", "rounds_flown", "rounds_flown");
	}

}
