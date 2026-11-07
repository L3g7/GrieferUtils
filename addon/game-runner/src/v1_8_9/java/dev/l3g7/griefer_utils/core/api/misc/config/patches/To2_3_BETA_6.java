/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.config.patches;

import dev.l3g7.griefer_utils.core.api.misc.config.ConfigPatcher.ConfigPatch;
import net.labymod.ingamechat.tools.filter.Filters;
import net.labymod.ingamechat.tools.filter.Filters.Filter;
import net.labymod.ingamegui.ModuleConfig;
import net.labymod.ingamegui.ModuleConfigElement;
import net.labymod.main.LabyMod;

import java.util.HashMap;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;

public class To2_3_BETA_6 extends ConfigPatch {

	@Override
	public boolean shouldApply() {
		return isConfigOlderThan("2.3-BETA-6") && LABY_3.isActive();
	}

	@Override
	public void patch() {
		// Renamed modules
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
		ModuleConfig.getConfigManager().save();

		// Migrated chat filters from ':' to '»'
		for (Filter filter : LabyMod.getInstance().getChatToolManager().getFilters()) {
			String[] containsNot = filter.getWordsContainsNot();
			if (containsNot.length == 0)
				continue;

			String[] newContainsNot = new String[containsNot.length];
			int idx = 0;
			boolean foundDelimiter = false;
			for (String s : filter.getWordsContainsNot()) {
				if (s.equals("»"))
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

		LabyMod.getInstance().getChatToolManager().saveTools();
	}

}
