/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.config.patches;

import dev.l3g7.griefer_utils.core.api.misc.config.ConfigPatcher.ConfigPatch;

public class To2_3 extends ConfigPatch {

	@Override
	public boolean shouldApply() {
		return isConfigOlderThan("2.3");
	}

	@Override
	public void patch() {
		// Introduced beta channel
		var autoUpdate = get("settings.auto_update");
		if (!autoUpdate.has("release_channel")) {
			boolean wasBeta = configVersion.toLowerCase().contains("beta") || configVersion.toLowerCase().contains("rc");
			autoUpdate.set("release_channel", wasBeta ? "BETA" : "STABLE");
		}
	}

}
