/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.config.patches;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.misc.config.ConfigPatcher.ConfigPatch;

public class To2_0 extends ConfigPatch {

	@Override
	public boolean shouldApply() {
		return isConfigOlderThan("2.0");
	}

	@Override
	public void patch() {
		// Revamped ChatReactor capture group support
		var chatReactor = get("chat.chat_reactor");
		if (chatReactor.has("entries")) {
			for (JsonObject entry : chatReactor.getObjectArray("entries")) {
				if (!entry.get("is_regex").getAsBoolean())
					continue;

				String command = entry.get("command").getAsString();
				command = command.replace("$", "$$");
				command = command.replaceAll("\\\\(\\d+)", "\\$$1");
				entry.addProperty("command", command);
			}
		}
	}

}
