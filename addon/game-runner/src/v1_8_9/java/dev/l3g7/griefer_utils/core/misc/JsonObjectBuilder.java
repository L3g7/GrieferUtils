/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonObjectBuilder {

	private final JsonObject object = new JsonObject();

	public JsonObjectBuilder add(String key, JsonElement value) {
		object.add(key, value);
		return this;
	}

	public JsonObjectBuilder add(String key, String value) {
		object.addProperty(key, value);
		return this;
	}

	public JsonObject build() {
		return object;
	}

}
