/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.modules.laby4.balances.inventory_value;

import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.features.modules.Laby4Module;
import dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.griefer_info.gui.GuiBigChest;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.player;
import static net.labymod.api.client.gui.hud.hudwidget.text.TextLine.State.HIDDEN;
import static net.labymod.api.client.gui.hud.hudwidget.text.TextLine.State.VISIBLE;

@Singleton
public class InventoryValue extends Laby4Module {

	private TextLine ownInventory, openInventory;

	private static final SwitchSetting auto = SwitchSetting.create()
		.name("Wert automatisch bestimmen")
		.description("Ob der Item-Wert automatisch bestimmt werden soll, oder ob nur Items mit einem manuell eingetragenen Wert gezählt werden sollen.")
		.defaultValue(true)
		.icon(Items.gold_ingot);

	private final ItemValueListSetting entries = new ItemValueListSetting()
		.name("Werte")
		.disableSubsettingConfig()
		.icon("coin_pile");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Inventar-Wert")
		.description("Zeigt dir an, wie viel ein Inventar wert ist.")
		.icon("chest")
		.subSettings(auto, HeaderSetting.create(), entries);

	@Override
	protected void createText() {
		ownInventory = createLine("Eigenes Inventar", "0$");
		openInventory = createLine("Geöffnetes Inventar", "0$");
	}

	@Override
	public void onTick(boolean isEditorContext) {
		if (player() == null) {
			ownInventory.updateAndFlush("0$");
			openInventory.updateAndFlush("0$");
			return;
		}

		ownInventory.updateAndFlush(getValue(Arrays.asList(player().inventory.mainInventory)));

		// Check if an inventory is open
		GuiScreen screen = mc().currentScreen;
		if (player() == null || !(screen instanceof GuiContainer) || screen instanceof GuiInventory || screen instanceof GuiBigChest) {
			openInventory.setState(HIDDEN);
			openInventory.updateAndFlush("0$");
			return;
		}

		// Update openInventory
		List<Slot> slots = ((GuiContainer) mc().currentScreen).inventorySlots.inventorySlots;
		slots = slots.subList(0, slots.size() - 9 * 4);

		openInventory.setState(VISIBLE);
		openInventory.updateAndFlush(getValue(slots.stream().map(Slot::getStack).collect(Collectors.toList())));
	}

	private String getValue(List<ItemStack> itemStacks) {
		long total = 0;

		itemLoop:
		for (ItemStack stack : itemStacks) {
			if (stack == null)
				continue;

			for (ItemValue value : entries.get()) {
				if (value.appliesTo(stack)) {
					total += (long) value.value * stack.stackSize;
					continue itemLoop;
				}
			}

			if (auto.get())
				total += ItemValue.autoDetect(stack) * stack.stackSize;
		}

		return Constants.DECIMAL_FORMAT_98.format(total) + "$";
	}

}
