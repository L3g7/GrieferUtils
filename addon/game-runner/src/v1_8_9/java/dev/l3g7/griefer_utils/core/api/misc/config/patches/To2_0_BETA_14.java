/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.config.patches;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.misc.config.ConfigPatcher.ConfigPatch;

public class To2_0_BETA_14 extends ConfigPatch {

	@Override
	public boolean shouldApply() {
		return isConfigOlderThan("2.0-BETA-14");
	}

	@Override
	public void patch() {
		// Renamed ChestSearch -> ItemSearch
		rename("world.chest_search", "world.item_search");

		// Add pages, non-command messages to CommandPieMenu
		var commandPieMenu = get("chat.command_pie_menu");
		if (commandPieMenu.has("entries")) {
			JsonArray pages = new JsonArray();
			JsonObject page = new JsonObject();
			page.addProperty("name", "Unbenannte Seite");

			for (JsonObject obj : commandPieMenu.getObjectArray("entries")) {
				String command = obj.get("command").getAsString();
				if (!command.startsWith("/"))
					command = "/" + command;

				obj.addProperty("command", command);
			}

			page.add("entries", commandPieMenu.get("entries"));
			pages.add(page);
			commandPieMenu.set("pages", pages);
		}
	}

}
