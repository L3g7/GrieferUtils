/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.config.patches;

import com.google.gson.JsonArray;
import dev.l3g7.griefer_utils.core.api.misc.config.ConfigPatcher.ConfigPatch;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.entry.ChatMenuEntry;

public class To2_4_8_BETA_1 extends ConfigPatch {

	@Override
	public boolean shouldApply() {
		return isConfigOlderThan("2.4.8-BETA-1");
	}

	@Override
	public void patch() {
		// Made ChatMenu default entries customizable
		var chatMenu = get("chat.ingoing.chat_menu");
		if (!chatMenu.has("entries") || !chatMenu.get("entries").isJsonObject())
			return;

		rename("chat.ingoing.chat_menu.entries.Text kopieren", "chat.ingoing.chat_menu.copy_text");

		JsonArray newEntries = new JsonArray();
		var entries = get("chat.ingoing.chat_menu.entries");
		for (ChatMenuEntry entry : ChatMenuEntry.DEFAULT_ENTRIES.get()) {
			if (entries.has(entry.name) && entries.get(entry.name).getAsBoolean()) {
				entry.enabled = true;
				newEntries.add(entry.encode());
			}
		}

		if (entries.has("custom"))
			newEntries.addAll(entries.getJsonArray("custom"));

		get("chat.ingoing.chat_menu").set("entries", newEntries);
	}

}
