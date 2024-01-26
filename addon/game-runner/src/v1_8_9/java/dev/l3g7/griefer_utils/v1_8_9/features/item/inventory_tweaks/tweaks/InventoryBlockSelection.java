/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.item.inventory_tweaks.tweaks;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.BlockPickEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.item.inventory_tweaks.InventoryTweaks;
import dev.l3g7.griefer_utils.v1_8_9.features.item.item_saver.specific_item_saver.ItemSaver;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.player;

@Singleton
public class InventoryBlockSelection extends InventoryTweaks.InventoryTweak {

	private final SwitchSetting compressed = SwitchSetting.create()
		.name("Komprimierte Items")
		.description("Ob komprimierte Items bei der Blockauswahl auch ausgewählt werden sollen.")
		.icon(Blocks.stone);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Blockauswahl mit Inventar")
		.description("Erweitert die Blockauswahl um das Inventar.")
		.icon("middle_click")
		.subSettings(compressed);

	@EventListener
	public void onBlockPick(BlockPickEvent event) {
		if (!enabled.get())
			return;

		InventoryPlayer inv = player().inventory;

		int targetSlot = -1;

		for(Supplier<Integer> slotSupplier : new Supplier[] {
			() -> getHotbarSlot(false, Objects::isNull), // Empty slot
			() -> getHotbarSlot(true, is -> is.getItem() instanceof ItemBlock), // Block
			() -> getHotbarSlot(true, is -> !is.isItemStackDamageable()), // Not a tool
			() -> getHotbarSlot(true, is -> true), // Not in the ItemSaver
			() -> getHotbarSlot(false, is -> !ItemSaver.getSetting(is).extremeDrop.get())}) { // Doesn't have extreme drop enabled
			targetSlot = slotSupplier.get();
			if (targetSlot != -1)
				break;
		}

		// All items had extreme drop enabled
		if (targetSlot == -1)
			return;

		int bestSlot = 0;
		int bestScore = -1;

		for (int slot = 0; slot < 36; slot++) {
			int score = getScore(event.requiredStack, slot);
			if (bestScore < score) {
				bestScore = score;
				bestSlot = slot;
			}
		}

		if (bestScore == -1 || inv.currentItem == bestSlot)
			return;

		if (bestSlot < 9) { // Best slot is in hotbar
			inv.currentItem = bestSlot;
			event.cancel();
			return;
		}

		// Move stack
		mc().playerController.windowClick(0, bestSlot, targetSlot, 2, player());

		if (inv.currentItem != targetSlot)
			inv.currentItem = targetSlot;

		event.cancel();
	}

	private int getScore(ItemStack requiredStack, int slot) {
		ItemStack stack = player().inventory.getStackInSlot(slot);
		if (stack == null || !stack.isItemEqual(requiredStack) || ItemSaver.getSetting(stack) != null)
			return -1;

		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("stackSize"))
			return Integer.MAX_VALUE;

		if (!compressed.get())
			return 0;

		if (!stack.getTagCompound().getBoolean("warningDisplayed"))
			return 0;

		String line = ItemUtil.getLoreAtIndex(stack, 0);
		return Integer.parseInt(line.substring(12).replace(".", ""));
	}

	private int getHotbarSlot(boolean prefilter, Predicate<ItemStack> filter) {
		ItemStack[] stacks = player().inventory.mainInventory;
		for (int i = 0; i < 9; i++) {
			if (prefilter && (stacks[i] == null || ItemSaver.getSetting(stacks[i]) != null))
				continue;

			if (filter.test(stacks[i]))
				return i;
		}

		return -1;
	}

}
