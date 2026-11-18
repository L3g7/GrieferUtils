/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Function;
import dev.l3g7.griefer_utils.core.api.misc.server.GUServer;
import dev.l3g7.griefer_utils.core.api.util.io.IO;

/**
 * Fired when {@link GUServer} loads the static API.
 */
public class StaticDataReceiveEvent extends Event {

	public final JsonObject data;

	public StaticDataReceiveEvent(JsonObject data) {
		this.data = data;
	}

	public <T> T read(String key, Class<T> type) {
		return read(key, json -> IO.GSON.fromJson(json, type));
	}

	public <T> T read(String key, Function<JsonElement, T> parser) {
		JsonElement value = data.get(key);
		if (value == null)
			throw new IllegalStateException("Static api doesn't have " + key);

		return parser.apply(value);
	}

}
