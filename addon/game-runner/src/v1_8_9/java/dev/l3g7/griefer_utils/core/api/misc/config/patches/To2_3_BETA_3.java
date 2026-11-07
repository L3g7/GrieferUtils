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

public class To2_3_BETA_3 extends ConfigPatch {

	@Override
	public boolean shouldApply() {
		return isConfigOlderThan("2.3-BETA-3");
	}

	@Override
	public void patch() {
		// Introduced Received module
		var money = get("modules.money");
		for (Entry<String, JsonElement> entry : money.getObject("balances")) {
			JsonObject balances = entry.getValue().getAsJsonObject();
			var newDst = get("modules.money.data." + entry.getKey());

			if (balances.has("spent") && !newDst.has("spent"))
				newDst.set("spent", balances.get("spent"));
		}
	}

}
