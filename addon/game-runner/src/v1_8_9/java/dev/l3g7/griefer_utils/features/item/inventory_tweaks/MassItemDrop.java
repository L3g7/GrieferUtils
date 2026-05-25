/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * This file has been modified by itzW0lf.
 */

package dev.l3g7.griefer_utils.features.item.inventory_tweaks;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.settings.types.KeySetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.getSlotUnderMouse;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
public class MassItemDrop extends Feature {

	private final SwitchSetting ignoreNbt = SwitchSetting.create()
		.name("NBT-Daten ignorieren")
		.description("Ignoriert NBT-Daten beim Vergleich — alle Items desselben Typs und Namens werden gedroppt, unabhängig von Verzauberungen oder anderen Daten.")
		.icon("enchanted_book");

	@MainElement
	private final KeySetting dropKey = KeySetting.create()
		.name("Alle gleichen Items droppen")
		.description("Drücke diese Taste, während du über einem Item im Inventar schwebst, um alle gleichartigen Items zu droppen.")
		.icon("dropper")
		.since("2.5.0")
		.subSettings(ignoreNbt)
		.triggersInContainers()
		.pressCallback(pressed -> {
			if (!pressed || !(mc().currentScreen instanceof GuiContainer gc))
				return;

			Slot hovered = getSlotUnderMouse(mc().currentScreen);
			if (hovered == null || !hovered.getHasStack())
				return;

			ItemStack target = hovered.getStack();
			int windowId = gc.inventorySlots.windowId;

			List<Integer> slotNumbers = new ArrayList<>();
			for (int i = 0; i < gc.inventorySlots.inventorySlots.size(); i++) {
				Slot slot = gc.inventorySlots.getSlot(i);
				if (slot.getHasStack() && matches(slot.getStack(), target))
					slotNumbers.add(slot.slotNumber);
			}

			for (int i = 0; i < slotNumbers.size(); i++) {
				int slotNumber = slotNumbers.get(i);
				TickScheduler.runAfterClientTicks(() ->
					mc().playerController.windowClick(windowId, slotNumber, 1, 4, mc().thePlayer), i * 2);
			}
		});

	private boolean matches(ItemStack a, ItemStack b) {
		if (a.getItem() != b.getItem())
			return false;
		if (a.getItemDamage() != b.getItemDamage())
			return false;
		if (!a.getDisplayName().equals(b.getDisplayName()))
			return false;
		if (ignoreNbt.get())
			return true;
		return ItemStack.areItemStackTagsEqual(a, b);
	}

}
