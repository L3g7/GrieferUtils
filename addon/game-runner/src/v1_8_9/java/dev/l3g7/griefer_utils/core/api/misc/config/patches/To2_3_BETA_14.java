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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map.Entry;

public class To2_3_BETA_14 extends ConfigPatch {

	@Override
	public boolean shouldApply() {
		return isConfigOlderThan("2.3-BETA-14");
	}

	@Override
	public void patch() {
		// Enforced CooldownNotification types
		List<String> validKeys = Arrays.asList("/premium", "/ultra", "/kopf", "/grieferboost", "/freekiste", "/startkick");
		for (Entry<String, JsonElement> entry : get("player.cooldown_notifications.end_dates")) {
			JsonObject data = entry.getValue().getAsJsonObject();
			for (Entry<String, JsonElement> key : new ArrayList<>(data.entrySet()))
				if (!validKeys.contains(key.getKey()))
					data.remove(key.getKey());
		}

		// Changed OrbStats stats encoding
		for (Entry<String, JsonElement> entry : get("modules.orb_stats.stats")) {
			JsonObject o = entry.getValue().getAsJsonObject();
			if (!o.has("data"))
				continue;

			String b64 = o.get("data").getAsString();

			ByteBuffer buf = ByteBuffer.wrap(Base64.getDecoder().decode(b64));
			ByteBuffer newBuf = ByteBuffer.allocate(buf.capacity() / 8 * 12);

			while (buf.hasRemaining()) {
				newBuf.putInt(buf.getInt());
				newBuf.putLong(buf.getInt());
			}

			o.addProperty("data", Base64.getEncoder().encodeToString(newBuf.array()));
		}
	}

}
