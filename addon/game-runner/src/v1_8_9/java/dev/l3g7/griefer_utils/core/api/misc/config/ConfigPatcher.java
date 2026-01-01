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
import dev.l3g7.griefer_utils.core.api.util.ArrayUtil;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;
import dev.l3g7.griefer_utils.core.api.util.StringUtil;
import net.labymod.ingamechat.tools.filter.Filters;
import net.labymod.ingamegui.ModuleConfig;
import net.labymod.ingamegui.ModuleConfigElement;
import net.labymod.main.LabyMod;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.api.misc.VersionComparator.VERSION_COMPARATOR;

public class ConfigPatcher {

	public static boolean versionChanged = false;

	JsonObject config;

	public ConfigPatcher(JsonObject config) {
		this.config = config;
	}

	private boolean isConfigOlderThan(String version) {
		return VERSION_COMPARATOR.compare(version, config.get("version").getAsString()) < 0;
	}

	public void patch() {
		if (!config.has("version")) {
			config.addProperty("version", labyBridge.addonVersion());
			return;
		}

		String versionInConfig = config.get("version").getAsString();
		String newVersion = labyBridge.addonVersion();
		if (!newVersion.equals(versionInConfig)) {
			config.addProperty("version", newVersion);
			versionChanged = true;
		}

		if (isConfigOlderThan("2.0-BETA-13.2")) {
			rename("item.inventory_tweaks.crafting_shift.craftingShift", "item.inventory_tweaks.better_shift.enabled");
		}

		if (isConfigOlderThan("2.0-BETA-14")) {
			String oldPath = "chat.command_pie_menu.entries";
			JsonArray entries = getParentOf(oldPath).getAsJsonArray("entries");
			if (entries != null) {
				JsonObject parent = getParentOf("chat.command_pie_menu.pages");
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

		if (isConfigOlderThan("2.0-RC-8")) {
			JsonObject parent = get("item.orb_saver");
			if (parent != null) {
				if (getBooleanValue(parent.get("enabled"))) {
					if (!getBooleanValue(parent.get("on_price_fall"))) {
						parent.addProperty("enabled", false);
					}
				}
			}
		}

		if (isConfigOlderThan("2.0-RC-9")) {
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

		if (isConfigOlderThan("2.0-RC-12")) {
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

		if (isConfigOlderThan("2.0")) {
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

		if (isConfigOlderThan("2.2-BETA-1")) {
			rename("chat.fix_ghost_blocks", "chat.ghost_blocks_fix");
		}

		if (isConfigOlderThan("2.2-BETA-6")) {
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

		if (isConfigOlderThan("2.3-BETA-3")) {
			JsonObject o = get("modules.money.balances");
			for (Entry<String, JsonElement> entry : o.entrySet()) {
				JsonObject balances = entry.getValue().getAsJsonObject();
				if (!balances.has("spent") || get("modules.money.data." + entry.getKey()).has("spent"))
					continue;

				get("modules.money.data." + entry.getKey()).add("spent", balances.get("spent"));
			}
		}

		if (isConfigOlderThan("2.3-BETA-6") && LABY_3.isActive()) {
			// Patch modules
			Map<String, String> map = new HashMap<>() {{
				for (String key : new String[]{
					"Bankguthaben", "Kontostand", "Inventar-Wert", "Ausgegeben", "Eingenommen", "Verdient", "Chatlog",
					"Clearlag", "MobRemover", "Orbtrank-Timer", "Orb-Statistik", "Orbguthaben", "Block-Infos",
					"Booster", "Fehlende Adv. Blöcke", "HeadOwner", "Rahmen im Chunk", "Redstone", "Server-Performance",
					"Spawn-Runden Zähler", "Spieler in der Nähe"
				}) {
					put(key, key);
				}

				put("BoosterL3", "Booster");
				put("BankBalance", "Bankguthaben");
				put("BlockInfo", "Block-Infos");
				put("BlockInfoL3", "Block-Infos");
				put("ClearLag", "Clearlag");
				put("CoinBalance", "Kontostand");
				put("Earned", "Verdient");
				put("InventoryValue", "Inventar-Wert");
				put("ItemFrameLimitIndicator", "Rahmen im Chunk");
				put("MissingAdventurerBlocks", "Fehlende Adv. Blöcke");
				put("NearbyPlayers", "Spieler in der Nähe");
				put("NearbyPlayersL3", "Spieler in der Nähe");
				put("OrbBalance", "Orbguthaben");
				put("OrbStats", "Orb-Statistik");
				put("PotionTimer", "Orbtrank-Timer");
				put("PotionTimerL3", "Orbtrank-Timer");
				put("Received", "Eingenommen");
				put("ServerPerformance", "Server-Performance");
				put("SpawnCounter", "Spawn-Runden Zähler");
				put("SpawnCounterL3", "Spawn-Runden Zähler");
				put("Spent", "Ausgegeben");
			}};

			Map<String, ModuleConfigElement> modules = ModuleConfig.getConfig().getModules();
			for (String oldName : map.keySet()) {
				ModuleConfigElement oldConfig = modules.remove(oldName);
				if (oldConfig == null)
					continue;

				ModuleConfigElement newConfig = modules.computeIfAbsent(map.get(oldName), k -> new ModuleConfigElement());

				for (int i = 0; i < 2; i++) {
					newConfig.setRegion(i, oldConfig.getRegions()[i]);
					newConfig.setAlignment(i, oldConfig.getAlignment(i));
					newConfig.setX(i, oldConfig.getX(i));
					newConfig.setY(i, oldConfig.getY(i));
				}
				newConfig.setEnabled(oldConfig.getEnabled());
				newConfig.setListedAfter(map.getOrDefault(oldConfig.getListedAfter(), oldConfig.getListedAfter()));
				newConfig.setLastListedAfter(oldConfig.getLastListedAfter());
				newConfig.setUseExtendedSettings(oldConfig.isUsingExtendedSettings());
				newConfig.setScale(oldConfig.getScale());
				newConfig.setAttributes(oldConfig.getAttributes());
			}
			// Save modules
			ModuleConfig.getConfigManager().save();

			// Patch filters
			for (Filters.Filter filter : LabyMod.getInstance().getChatToolManager().getFilters()) {
				String[] containsNot = filter.getWordsContainsNot();
				if (containsNot.length == 0)
					continue;

				String[] newContainsNot = new String[containsNot.length];
				int idx = 0;
				boolean foundDelimiter = false;
				for (String s : filter.getWordsContainsNot()) {
					if (s.equals("\u00bb"))
						foundDelimiter = true;
					if (!s.equals(":"))
						newContainsNot[idx++] = s;
				}

				if (foundDelimiter) {
					String[] newContainsNotCopy = new String[idx];
					System.arraycopy(newContainsNot, 0, newContainsNotCopy, 0, idx);
					filter.setWordsContainsNot(newContainsNotCopy);
				}
			}

			// Save filters
			LabyMod.getInstance().getChatToolManager().saveTools();
		}

		if (isConfigOlderThan("2.3-BETA-8") && LABY_3.isActive()) {

			// Patch filters
			for (Filters.Filter filter : LabyMod.getInstance().getChatToolManager().getFilters()) {
				String[] containsNot = filter.getWordsContainsNot();
				if (containsNot.length == 0)
					continue;

				String[] newContainsNot = new String[containsNot.length];
				int idx = 0;
				for (String s : filter.getWordsContainsNot())
					if (s != null)
						newContainsNot[idx++] = s;

				String[] newContainsNotCopy = new String[idx];
				System.arraycopy(newContainsNot, 0, newContainsNotCopy, 0, idx);
				filter.setWordsContainsNot(newContainsNotCopy);
			}

			// Save filters
			LabyMod.getInstance().getChatToolManager().saveTools();
		}

		if (isConfigOlderThan("2.3-BETA-14")) {
			String[] validKeys = new String[]{"/premium", "/ultra", "/kopf", "/grieferboost", "/freekiste", "/startkick"};
			for (Entry<String, JsonElement> entry : get("player.cooldown_notifications.end_dates").entrySet()) {
				JsonObject data = entry.getValue().getAsJsonObject();
				for (Entry<String, JsonElement> key : new ArrayList<>(data.entrySet()))
					if (Arrays.binarySearch(validKeys, key.getKey()) < 0)
						data.remove(key.getKey());
			}

			JsonObject oldStats = get("modules.orb_stats.stats");
			for (Entry<String, JsonElement> entry : oldStats.entrySet()) {
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

		if (versionInConfig.equals("2.3-BETA-15")) {
			for (String key : new String[]{"chat", "item", "render", "player", "world"}) {
				JsonObject root = get(key).getAsJsonObject();
				if (get(key + ".active").isJsonObject()) {
					merge(root, get(key + ".active").getAsJsonObject());
					set(key + ".active", new JsonPrimitive(true));
				}
			}
		}

		if (isConfigOlderThan("2.3-BETA-18")) {
			JsonObject autoUnnick = getParentOf("chat.auto_unnick.tab");
			if (!autoUnnick.has("tab"))
				autoUnnick.addProperty("tab", false);
		}

		if (isConfigOlderThan("2.3-BETA-24"))
			rename("chat.filter_webhooks.filter", "chat.filter_webhooks.filters.laby3");

		if (isConfigOlderThan("2.3")) {
			JsonObject autoUpdate = getParentOf("settings.auto_update.release_channel");
			if (!autoUpdate.has("release_channel")) {
				boolean wasBeta = versionInConfig.toLowerCase().contains("beta") || versionInConfig.toLowerCase().contains("rc");
				autoUpdate.addProperty("release_channel", wasBeta ? "BETA" : "STABLE");
			}
		}
	}

	protected void rename(String oldKey, String newKey) {
		JsonObject oldParent = getParentOf(oldKey);
		JsonObject newParent = getParentOf(newKey);

		if (oldParent.get(getKey(oldKey)) != null)
			newParent.add(getKey(newKey), oldParent.get(getKey(oldKey)));
	}

	private JsonObject get(String path) {
		return getParentOf(path + ".,");
	}

	private JsonObject getParentOf(String child) {
		String[] parts = child.split("\\.");
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
		getParentOf(path).add(getKey(path), value);
	}

	private boolean getBooleanValue(JsonElement element) {
		return element != null && element.getAsBoolean();
	}

	private void merge(JsonObject target, JsonObject data) {
		for (Entry<String, JsonElement> entry : data.entrySet()) {
			if (entry.getValue().isJsonObject() && target.has(entry.getKey())) {
				merge(target.get(entry.getKey()).getAsJsonObject(), entry.getValue().getAsJsonObject());
			} else {
				target.add(entry.getKey(), entry.getValue());
			}
		}
	}

}
