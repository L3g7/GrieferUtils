/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.misc.gui.guis.GuiBigChest;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;

@Singleton
public class BetterPlotMenu extends Feature {

	private int currentWindowId = -1;
	private GuiPlots currentGuiPlots = null;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("/zuhause verbessern")
		.description("Ersetzt die Äxte im /zuhause-Menü durch die Blöcke der Citybuilds und behebt die Anzahl.")
		.icon("XZRF:region_map");

	@EventListener
	private void onGuiOpen(PacketReceiveEvent<S2DPacketOpenWindow> event) {
		if (!event.packet.getWindowTitle().getFormattedText().startsWith("Grundstücke")) {
			currentWindowId = -1;
			return;
		}

		event.cancel();
		currentWindowId = event.packet.getWindowId();
		currentGuiPlots = new GuiPlots();
		currentGuiPlots.open();
	}

	private static final class GuiPlots extends GuiBigChest {

		private final ItemStack[] itemStacks = new ItemStack[25];

		public GuiPlots() {
			super("Grundstücke", 5);
			for (int i = 0; i < itemStacks.length; i++) {
				Citybuild citybuild = Citybuild.values()[i + 1 /* Skip ANY */];
				itemStacks[i] = citybuild.toItemStack();
				addItem(i * 2 - i / 5, itemStacks[i], null);
				if (citybuild == Citybuild.CBE)
					break;
			}
		}

	}

	@EventListener
	private void onGuiSetSlot(PacketReceiveEvent<S2FPacketSetSlot> event) {
		if (currentGuiPlots == null || event.packet.func_149175_c() != currentWindowId)
			return;

		ItemStack stack = event.packet.func_149174_e();
		Citybuild cb = Citybuild.getCitybuild(stack.getDisplayName().replaceAll("§.", ""));
		if (cb == Citybuild.ANY)
			return;

		String lore = ItemUtil.getLoreAtIndex(stack, 0);
		if (!lore.endsWith("Grundstücke"))
			return;

		String plotAmount = lore.substring(2, lore.length() - " Grundstücke".length());
		ItemStack targetStack = currentGuiPlots.itemStacks[cb.ordinal() - 1];
		targetStack.stackSize = Integer.parseInt(plotAmount)*10;
		targetStack.setTagCompound(stack.getTagCompound());
	}

}
