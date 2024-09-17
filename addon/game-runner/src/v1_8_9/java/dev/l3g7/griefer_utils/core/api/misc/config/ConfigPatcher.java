/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.core.api.misc.VersionComparator;
import dev.l3g7.griefer_utils.core.api.util.ArrayUtil;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;
import dev.l3g7.griefer_utils.core.api.util.StringUtil;
import net.labymod.ingamegui.ModuleConfig;
import net.labymod.ingamegui.ModuleConfigElement;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;

public class ConfigPatcher {

	public static boolean versionChanged = false;

	JsonObject config;

	public ConfigPatcher(JsonObject config) {
		this.config = config;
	}

	public void patch() {
		if (!config.has("version")) {
			config.addProperty("version", labyBridge.addonVersion());
			return;
		}

		String version = config.get("version").getAsString();
		String newVersion = labyBridge.addonVersion();
		if (!newVersion.equals(version)) {
			config.addProperty("version", newVersion);
			versionChanged = true;
		}

		VersionComparator cmp = new VersionComparator();

		if (cmp.compare("2.0-BETA-13.2", version) < 0) {
			rename("item.inventory_tweaks.crafting_shift.craftingShift", "item.inventory_tweaks.better_shift.enabled");
		}

		if (cmp.compare("2.0-BETA-14", version) < 0) {
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

		if (cmp.compare("2.0-RC-8", version) < 0) {
			JsonObject parent = get("item.orb_saver");
			if (parent != null) {
				if (getBooleanValue(parent.get("enabled"))) {
					if (!getBooleanValue(parent.get("on_price_fall"))) {
						parent.addProperty("enabled", false);
					}
				}
			}
		}

		if (cmp.compare("2.0-RC-9", version) < 0) {
			rename("modules.block_preview.show_coordinates", "modules.block_info.show_coords");
			rename("modules.tps", "modules.server_performance");
			rename("modules.spawn_counter.roundsRan", "modules.spawn_counter.rounds_ran");
			rename("modules.spawn_counter.roundsFlown", "modules.spawn_counter.rounds_flown");
			rename("modules.orb_potion_timer", "modules.potion_timer");

			Optional<JsonObject> optional = IOUtil.read(new File("LabyMod/modules.json")).asJsonObject();
			if (optional.isPresent()) {
				JsonObject modules = optional.get().getAsJsonObject("modules");

				for (String file : FileProvider.getFiles(f -> f.startsWith("dev/l3g7/griefer_utils/features/modules/") && f.endsWith(".class"))) {
					ClassMeta meta = FileProvider.getClassMeta(file, true);
					if (meta != null && meta.hasSuperClass("dev/l3g7/griefer_utils/features/Module")) {
						String name = meta.name.substring(meta.name.lastIndexOf('/') + 1);
						JsonObject module = modules.getAsJsonObject(name);
						if (module != null && module.getAsJsonArray("enabled").size() > 0) {
							String key = String.format("modules.%s.enabled", StringUtil.convertCasing(name));
							set(key, new JsonPrimitive(true));
						}
					}
				}

			}
		}

		if (cmp.compare("2.0-RC-12", version) < 0) {
			rename("world.show_spawner_icons", "world.better_spawners.show_spawner_icons");
			JsonObject betterSpawners = get("world.better_spawners");

			JsonObject parent = get("world.spawner_with_held_item_fix");
			if (parent != null && getBooleanValue(parent.get("enabled"))) {
				betterSpawners.addProperty("enabled", true);
				betterSpawners.addProperty("spawner_with_held_item_fix", true);
			}

			parent = get("world.show_spawner_icons");
			if (parent != null && getBooleanValue(parent.get("enabled"))) {
				betterSpawners.addProperty("enabled", true);
			}
		}

		if (cmp.compare("2.0", version) < 0) {
			JsonObject chatReactor = get("chat.chat_reactor");
			if (chatReactor.has("entries")) {
				JsonArray entries = chatReactor.getAsJsonArray("entries");
				for (JsonElement e : entries) {
					JsonObject entry = e.getAsJsonObject();
					if (!entry.get("is_regex").getAsBoolean())
						continue;

					String command = entry.get("command").getAsString();
					command = command.replace("$", "$$");
					command = command.replaceAll("\\\\(\\d+)", "\\$$1");
					entry.addProperty("command", command);
				}
			}
		}

		if (cmp.compare("2.2-BETA-1", version) < 0) {
			rename("chat.fix_ghost_blocks", "chat.ghost_blocks_fix");
		}

		if (cmp.compare("2.2-BETA-6", version) < 0) {
			JsonElement element = get("item.recraft").get("key");
			if (element != null) {
				JsonObject object = null;
				if (element.isJsonObject()) {
					object = element.getAsJsonObject();
				} else if (element.isJsonArray()) {
					object = new JsonObject();
					object.add("value", element);
				}
				set("item.recraft.repeat_last_recording", object);
				get("item.recraft").remove("key");
			}
		}

		if (cmp.compare("2.3-BETA-3", version) < 0) {
			JsonObject o = get("modules.money.balances");
			for (Entry<String, JsonElement> entry : o.entrySet()) {
				JsonObject balances = entry.getValue().getAsJsonObject();
				if (!balances.has("spent") || get("modules.money.data." + entry.getKey()).has("spent"))
					continue;

				get("modules.money.data." + entry.getKey()).add("spent", balances.get("spent"));
			}
		}

		if (cmp.compare("2.3-BETA-6", version) < 0 && LABY_3.isActive()) {
			Map<String, String> map = new HashMap<>() {{
				put("BankBalance", "Bankguthaben");
				put("BlockInfo", "Block-Infos");
				put("ClearLag", "Clearlag");
				put("CoinBalance", "Kontostand");
				put("Earned", "Verdient");
				put("InventoryValue", "Inventar-Wert");
				put("ItemFrameLimitIndicator", "Rahmen im Chunk");
				put("MissingAdventurerBlocks", "Fehlende Adv. Blöcke");
				put("NearbyPlayers", "Spieler in der Nähe");
				put("OrbBalance", "Orbguthaben");
				put("OrbStats", "Orb-Statistik");
				put("PotionTimer", "Orbtrank-Timer");
				put("Received", "Eingenommen");
				put("ServerPerformance", "Server-Performance");
				put("SpawnCounter", "Spawn-Runden Zähler");
				put("Spent", "Ausgegeben");
			}};

			Map<String, ModuleConfigElement> modules = ModuleConfig.getConfig().getModules();
			for (String oldName : map.keySet()) {
				ModuleConfigElement oldConfig = modules.remove(oldName);
				if (oldConfig == null)
					return;

				ModuleConfigElement newConfig = modules.computeIfAbsent(map.get(oldName), k -> new ModuleConfigElement());

				for (int i = 0; i < 2; i++) {
					newConfig.setRegion(i, oldConfig.getRegions()[i]);
					newConfig.setAlignment(i, oldConfig.getAlignment(i));
					newConfig.setX(i, oldConfig.getX(i));
					newConfig.setY(i, oldConfig.getY(i));
				}
				newConfig.setEnabled(oldConfig.getEnabled());
				newConfig.setListedAfter(oldConfig.getListedAfter());
				newConfig.setLastListedAfter(oldConfig.getLastListedAfter());
				newConfig.setUseExtendedSettings(oldConfig.isUsingExtendedSettings());
				newConfig.setScale(oldConfig.getScale());
				newConfig.setAttributes(oldConfig.getAttributes());
			}
		}
	}

	private void rename(String oldKey, String newKey) {
		JsonObject oldParent = getParent(oldKey);
		JsonObject newParent = getParent(newKey);

		if (oldParent.get(getKey(oldKey)) != null)
			newParent.add(getKey(newKey), oldParent.get(getKey(oldKey)));
	}

	private JsonObject get(String path) {
		return getParent(path + ".,");
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

	private boolean getBooleanValue(JsonElement element) {
		return element != null && element.getAsBoolean();
	}

}