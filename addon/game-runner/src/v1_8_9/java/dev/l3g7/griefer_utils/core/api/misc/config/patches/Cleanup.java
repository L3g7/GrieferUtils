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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Removes empty paths.
 */
public class Cleanup extends ConfigPatch {

	@Override
	public boolean shouldApply() {
		return true;
	}

	@Override
	public void patch() {
		cleanup(config);
	}

	/**
	 * @return true if the element can be dissolved.
	 */
	private boolean cleanup(JsonElement element) {
		return switch (element) {
			case JsonObject obj -> {
				for (Entry<String, JsonElement> entry : new ArrayList<>(obj.entrySet()))
					if (cleanup(entry.getValue()))
						obj.remove(entry.getKey());

				yield obj.entrySet().isEmpty();
			}
			case JsonArray array -> {
				for (Iterator<JsonElement> iterator = array.iterator(); iterator.hasNext(); )
					if (cleanup(iterator.next()))
						iterator.remove();

				//noinspection SizeReplaceableByIsEmpty
				yield array.size() == 0;
			}
			case null, default -> false;
		};
	}

}
