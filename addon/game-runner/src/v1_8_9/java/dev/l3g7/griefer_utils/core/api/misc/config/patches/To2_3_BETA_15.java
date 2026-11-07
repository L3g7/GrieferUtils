/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.config.patches;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.misc.config.ConfigPatcher.ConfigPatch;

import java.util.Map.Entry;

public class To2_3_BETA_15 extends ConfigPatch {

	@Override
	public boolean shouldApply() {
		return configVersion.equals("2.3-BETA-15");
	}

	@Override
	public void patch() {
		// Normalized config
		for (String key : new String[]{"chat", "item", "render", "player", "world"}) {
			var category = get(key);
			if (category.has("active") && category.get("active").isJsonObject()) {
				merge(category.unwrap(), category.getObject("active").unwrap());
				category.set("active", true);
			}
		}
	}

	private static void merge(JsonObject target, JsonObject data) {
		for (Entry<String, JsonElement> entry : data.entrySet()) {
			if (entry.getValue().isJsonObject() && target.has(entry.getKey()))
				merge(target.getAsJsonObject(entry.getKey()), entry.getValue().getAsJsonObject());
			else
				target.add(entry.getKey(), entry.getValue());
		}
	}

}
