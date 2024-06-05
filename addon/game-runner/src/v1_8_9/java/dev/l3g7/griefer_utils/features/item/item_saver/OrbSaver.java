/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.item_saver;


import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.GuiModifyItemsEvent;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.features.item.item_saver.ItemSaverCategory.ItemSaver;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

@Singleton
public class OrbSaver extends ItemSaver {

	private static final ItemStack priceFellStack;
	private boolean clicking = false;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Orb-Saver")
		.description("Deaktiviert Abgeben von Items beim Orbhändler, wenn der Preis des Items gefallen ist.")
		.icon(ItemUtil.createItem(Blocks.gold_block, 0, true));

	@EventListener
	private void onGuiSetItems(GuiModifyItemsEvent event) {
		if (!event.getTitle().startsWith("§6Orbs - Verkauf "))
			return;

		ItemStack lastBarItem = event.getItem(44);
		if (lastBarItem == null || lastBarItem.getMetadata() != 5)
			return;

		ItemStack blockStack = priceFellStack.copy();

		String singlePrice = getOrbPrice(event.getItem(11));
		if (singlePrice != null) {
			String allItemsPrice = getOrbPrice(event.getItem(15));
			String secondLine = String.format("§7Klicke mit dem Mausrad, um die Items trotzdem für je §e%s §7Orbs (= §e%s §7Orbs) abzugeben.", singlePrice, allItemsPrice);
			ItemUtil.setLore(blockStack, "§cDer Preis ist gefallen!", secondLine);
		}

		for (int i = 0; i < 54; i++)
			if (i != 45)
				event.setItem(i, blockStack);
	}

	@EventListener
	private void onWindowClick(WindowClickEvent event) {
		if (clicking)
			return;

		if (!ItemUtil.getLoreAtIndex(event.itemStack, 0).equals("§cDer Preis ist gefallen!"))
			return;

		event.cancel();

		if (event.mode == 3) {
			clicking = true;
			mc().playerController.windowClick(event.windowId, 15, 0, 0, player());
			clicking = false;
		}
	}

	private static String getOrbPrice(ItemStack stack) {
		String price = ItemUtil.getLoreAtIndex(stack, 0);
		if (!price.endsWith("Orbs zu verkaufen."))
			return null;

		price = price.substring(price.lastIndexOf("§e"));
		price = price.substring(2, price.indexOf(' '));
		return price;
	}

	static {
		priceFellStack = ItemUtil.createItem(Blocks.stained_glass_pane, 14, "§c§lGeblockt!");
		ItemUtil.setLore(priceFellStack, "§cDer Preis ist gefallen!", "§7Klicke mit dem Mausrad, um die Items trotzdem abzugeben.");
	}

}
