/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.config.patches;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.misc.config.ConfigPatcher.ConfigPatch;

public class To2_2_BETA_6 extends ConfigPatch {

	@Override
	public boolean shouldApply() {
		return isConfigOlderThan("2.2-BETA-6");
	}

	@Override
	public void patch() {
		// Normalized config
		var recraft = get("item.recraft");
		var element = recraft.get("key");
		if (element != null) {
			if (element.isJsonObject()) {
				recraft.set("repeat_last_recording", element.getAsJsonObject());
			} else if (element.isJsonArray()) {
				JsonObject object = new JsonObject();
				object.add("value", element);
				recraft.set("repeat_last_recording", object);
			}

			recraft.remove("key");
		}
	}

}
