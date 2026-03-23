/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.gui.griefer_games;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.api.util.StringUtil;
import dev.l3g7.griefer_utils.core.events.GuiModifyItemsEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.item.ItemStack;

@Singleton
public class BetterHomeMenu extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("/homes verbessern")
		.description("Ersetzt die Gras-Blöcke im /home-Menü durch die der Citybuilds.")
		.icon("filled_map");

	@EventListener
	private void onGuiModify(GuiModifyItemsEvent event) {
		if (!event.getTitle().startsWith("§6Home-Punkte"))
			return;

		for (int i = 0; i < 28; i++) {
			int x = i / 7 + 1;
			int y = i % 7 + 1;

			ItemStack stack = event.getItem(x * 9 + y);
			if (stack == null || !stack.getDisplayName().startsWith("§6Home"))
				continue;

			String citybuild = ItemUtil.getLoreAtIndex(stack, 1).substring("§7Citybuild: §e".length());
			ItemStack cbStack = Citybuild.getCitybuild(citybuild).toItemStack();
			stack.setItem(cbStack.getItem());
			stack.setItemDamage(cbStack.getItemDamage());

			if (StringUtil.isNumeric(citybuild))
				stack.stackSize = Integer.parseInt(citybuild);
		}
	}

}
