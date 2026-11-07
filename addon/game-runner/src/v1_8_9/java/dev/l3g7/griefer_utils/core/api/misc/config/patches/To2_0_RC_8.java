/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.config.patches;

import dev.l3g7.griefer_utils.core.api.misc.config.ConfigPatcher.ConfigPatch;

public class To2_0_RC_8 extends ConfigPatch {

	@Override
	public boolean shouldApply() {
		return isConfigOlderThan("2.0-RC-8");
	}

	@Override
	public void patch() {
		// Dissolved OrbSaver onPriceFail setting
		var parent = get("item.orb_saver");
		if (parent.isTrue("enabled") && !parent.isTrue("on_price_fall"))
			parent.set("enabled", false);
	}

}
