/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.core.misc.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.misc.VersionComparator;
import dev.l3g7.griefer_utils.core.util.ArrayUtil;
import dev.l3g7.griefer_utils.util.AddonUtil;

public class ConfigPatcher {

	JsonObject config;

	public ConfigPatcher(JsonObject config) {
		this.config = config;
	}

	public void patch() {
		if (!config.has("version")) {
			config.addProperty("version", AddonUtil.getVersion());
			return;
		}

		String version = config.get("version").getAsString();
		config.addProperty("version", AddonUtil.getVersion());

		VersionComparator cmp = new VersionComparator();

		if (cmp.compare("2.0-BETA-13.2", version) < 0) {
			rename("item.inventory_tweaks.crafting_shift.craftingShift", "item.inventory_tweaks.better_shift.enabled");
		}

		if (cmp.compare("2.0-BETA-14", version) <= 0) {
			String oldPath = "chat.command_pie_menu.entries";
			JsonArray entries = getParent(oldPath).getAsJsonArray("entries");
			if (entries != null) {
				JsonObject parent = getParent("chat.command_pie_menu.pages");
				JsonArray pages = new JsonArray();
				JsonObject page = new JsonObject();
				page.addProperty("name", "Unbenannte Seite");

				for (JsonElement entry : entries) {
					JsonObject obj = entry.getAsJsonObject();
					String command = obj.get("command").getAsString();
					if (!command.startsWith("/"))
						command = "/" + command;
					obj.addProperty("command", command);
				}

				page.add("entries", entries);
				pages.add(page);
				parent.add("pages", pages);
			}

			rename("world.chest_search", "world.item_search");
		}
	}

	private void rename(String oldKey, String newKey) {
		JsonObject oldParent = getParent(oldKey);
		JsonObject newParent = getParent(newKey);

		if (oldParent.get(getKey(oldKey)) != null)
			newParent.add(getKey(newKey), oldParent.get(getKey(oldKey)));
	}

	private JsonObject getParent(String path) {
		String[] parts = path.split("\\.");
		JsonObject obj = config;
		for (int i = 0; i < parts.length - 1; i++) {
			if (!obj.has(parts[i]) || !(obj.get(parts[i]).isJsonObject()))
				obj.add(parts[i], new JsonObject());
			obj = obj.get(parts[i]).getAsJsonObject();
		}
		return obj;
	}

	private String getKey(String path) {
		return ArrayUtil.last(path.split("\\."));
	}

	private void set(String path, JsonElement value) {
		getParent(path).add(getKey(path), value);
	}

}