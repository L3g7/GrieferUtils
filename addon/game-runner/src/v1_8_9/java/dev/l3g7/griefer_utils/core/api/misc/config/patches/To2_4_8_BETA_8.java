/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.config.patches;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.misc.config.ConfigPatcher.ConfigPatch;

import java.util.Map;

public class To2_4_8_BETA_8 extends ConfigPatch {

	@Override
	public boolean shouldApply() {
		return isConfigOlderThan("2.4.8-BETA-8");
	}

	@Override
	public void patch() {
		// Migrated SpecificItemSaver to ListSetting
		var itemSaver = get("item.item_saver.specific_item_saver");
		if (itemSaver.has("entries") && itemSaver.get("entries").isJsonObject()) {
			JsonArray newEntries = new JsonArray();

			for (Map.Entry<String, JsonElement> entry : itemSaver.getObject("entries")) {
				JsonObject oldEntry = entry.getValue().getAsJsonObject();
				JsonObject newEntry = new JsonObject();

				newEntry.addProperty("nbt", entry.getKey());
				newEntry.add("name", oldEntry.get("name"));

				newEntry.add("deny_drop", oldEntry.get("drop"));
				newEntry.add("deny_item_pickup", oldEntry.get("extreme_drop"));
				newEntry.add("deny_left_click", oldEntry.get("leftclick"));
				newEntry.add("deny_right_click", oldEntry.get("rightclick"));

				newEntries.add(newEntry);
			}

			itemSaver.set("entries", newEntries);
		}

		// Migrated ToolSaver to ListSetting
		var toolSaver = get("item.item_saver.tool_saver");
		if (toolSaver.has("exclusions") && toolSaver.get("exclusions").isJsonObject()) {
			JsonArray newExclusions = new JsonArray();

			for (Map.Entry<String, JsonElement> entry : toolSaver.getObject("exclusions")) {
				JsonObject exclusion = new JsonObject();
				exclusion.addProperty("nbt", entry.getKey());
				exclusion.add("name", entry.getValue());
				newExclusions.add(exclusion);
			}

			toolSaver.set("exclusions", newExclusions);
		}

		// Migrated InventoryValue to ListSetting
		var inventoryValue = get("modules.inventory_value");
		if (inventoryValue.has("entries") && inventoryValue.get("entries").isJsonObject()) {
			JsonArray newEntries = new JsonArray();

			for (Map.Entry<String, JsonElement> entry : inventoryValue.getObject("entries")) {
				JsonObject exclusion = new JsonObject();
				exclusion.addProperty("nbt", entry.getKey());
				exclusion.add("value", entry.getValue());
				newEntries.add(exclusion);
			}

			inventoryValue.set("entries", newEntries);
		}

		// Removed show_spawner_icons range from BetterSpawners
		var betterSpawners = get("world.better_spawners");
		if (betterSpawners.has("show_spawner_icons") && betterSpawners.get("show_spawner_icons").isJsonObject()) {
			boolean value = betterSpawners.getObject("show_spawner_icons").isTrue("value");
			betterSpawners.set("show_spawner_icons", value);
		}
	}

}
