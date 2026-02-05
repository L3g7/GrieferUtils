/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.botd;

import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.init.Blocks;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
public class BlockOfTheDayMessage extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Block des Tages-Nachricht")
		.description("Zeigt ein Popup an, wenn ein Block des Tages gefunden wurde.")
		.icon(ItemUtil.createItem(Blocks.glowstone, 0, true));

	public static void onBotd() {
		if (!FileProvider.getSingleton(BlockOfTheDayMessage.class).isEnabled())
			return;

		mc().ingameGUI.displayTitle("§aBlock des Tages", null, -1, -1, -1);
		mc().ingameGUI.displayTitle(null, "§fDu hast einen §aBlock des Tages §fgefunden!", -1, -1, -1);
		mc().ingameGUI.displayTitle(null, null, 0, 50, 10);
	}

}
