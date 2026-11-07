/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.config.patches;

import dev.l3g7.griefer_utils.core.api.misc.config.ConfigPatcher.ConfigPatch;

public class To2_4_BETA_1 extends ConfigPatch {

	@Override
	public boolean shouldApply() {
		return isConfigOlderThan("2.4-BETA-1");
	}

	@Override
	public void patch() {
		// Normalized config
		rename("item.inventory_tweaks.block_refill", "refillBlocks", "enabled");
	}

}
