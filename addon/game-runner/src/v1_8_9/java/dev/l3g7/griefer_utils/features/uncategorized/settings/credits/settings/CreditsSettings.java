/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.settings.credits.settings;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

@Bridged
public interface CreditsSettings {

	CreditsSettings creditsSettings = FileProvider.getBridge(CreditsSettings.class);

	void addTeam(List<BaseSetting<?>> elements);

	BaseSetting<?> createTextSetting(String... text);

	BaseSetting<?> createCookieSetting();

	BaseSetting<?> createUserSetting();

	static void giveCookie() {
		if (player() == null) {
			labyBridge.notify("§6Keks", "§eDu musst ingame sein!");
			return;
		}

		String nbt = "{id:\"minecraft:cookie\",Count:1b,tag:{display:{Lore:[\"\",\"§f§lGuten Appetit!\",\"§7Signiert von §aGrieferUtils §7am §e%s\"],Name:\"§6§lKeks\"}},Damage:0s}";
		nbt = String.format(nbt, new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
		boolean success = player().inventory.addItemStackToInventory(ItemUtil.fromNBT(nbt));
		labyBridge.notify("§6Keks", success ? "Guten Appetit!" : "§eDu musst Platz im Inventar haben!");
		MinecraftUtil.closeClientsideGUI();
	}

}
