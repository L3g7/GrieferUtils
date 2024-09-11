/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.inventory_tweaks.tweaks;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.WindowClickEvent;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.item.inventory_tweaks.InventoryTweaks;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import org.lwjgl.input.Keyboard;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

@Singleton
public class BetterShift extends InventoryTweaks.InventoryTweak {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Besseres Shiften")
		.description("""
			Ermöglicht das Verschieben von Items "Shift + Klick" in einigen Eingaben.
			- Werkbank
			- Spieler-Crafting §o(Shift + Alt + Klick)§r
			- Dorfbewohner""")
		.icon(Blocks.crafting_table);

	@EventListener
	public void onGuiCraftingClick(WindowClickEvent event) {
		if (!enabled.get() || !(mc().currentScreen instanceof GuiCrafting))
			return;

		if (event.mode != 1 || event.slotId <= 9 || event.slotId > 45)
			return;

		move(10, event);
	}

	@EventListener
	public void onGuiInvClick(WindowClickEvent event) {
		if (!enabled.get() || !(mc().currentScreen instanceof GuiInventory))
			return;

		if (event.mode != 1 || event.slotId <= 8 || ! Keyboard.isKeyDown(Keyboard.KEY_LMENU))
			return;

		move(5, event);
	}

	@EventListener
	public void onGuiVillagerClick(WindowClickEvent event) {
		if (!enabled.get() || !(mc().currentScreen instanceof GuiMerchant screen) || screen.getMerchant().getRecipes(player()) == null)
			return;

		if (event.mode != 1 || event.slotId <= 2)
			return;

		Container slots = screen.inventorySlots;

		ItemStack movedStack = slots.getSlot(event.slotId).getStack();
		if (movedStack == null)
			return;

		if (fillsUpSlot(0, slots, event) || fillsUpSlot(1, slots, event))
			return;

		// Check if a slot is free
		ItemStack firstStack = slots.getSlot(0).getStack();
		if (firstStack != null && slots.getSlot(1).getHasStack())
			return;

		boolean hasPossibleTrade = false;

		for (MerchantRecipe recipe : screen.getMerchant().getRecipes(player())) {
			if (firstStack != null) {
				// Check if the trade is possible with the provided given input
				if ((!firstStack.isItemEqual(recipe.getItemToBuy()) && !firstStack.isItemEqual(recipe.getSecondItemToBuy())) || !recipe.hasSecondItemToBuy())
					continue;
			}

			// Check if the moved stack is required in the trade
			if (!movedStack.isItemEqual(recipe.getItemToBuy())
				&& !movedStack.isItemEqual(recipe.getSecondItemToBuy()))
				continue;

			hasPossibleTrade = true;
		}

		if (!hasPossibleTrade)
			return;

		event.cancel();

		click(event, event.slotId);
		click(event, firstStack == null ? 0 : 1);
	}

	private boolean fillsUpSlot(int targetSlot, Container slots, WindowClickEvent event) {
		ItemStack movedStack = slots.getSlot(event.slotId).getStack();
		ItemStack targetStack = slots.getSlot(targetSlot).getStack();

		if (targetStack == null || targetStack.getItem() != movedStack.getItem() || targetStack.stackSize >= targetStack.getMaxStackSize())
			return false;

		if (targetStack.getItem().isDamageable() && targetStack.getItemDamage() != movedStack.getItemDamage())
			return false;

		click(event, event.slotId);
		click(event, targetSlot);

		// Check if item is consumed fully
		if (targetStack.getMaxStackSize() - targetStack.stackSize < movedStack.stackSize)
			TickScheduler.runAfterClientTicks(() -> click(event, event.slotId), 3);

		event.cancel();

		return true;
	}

	private void move(int end, WindowClickEvent event) {
		GuiContainer screen = (GuiContainer) mc().currentScreen;
		ItemStack movedStack = getStackUnderMouse(screen);
		if (movedStack == null)
			return;

		int targetSlot = -1;
		for (int i = 1; i < end; i++) {
			Slot slot = screen.inventorySlots.getSlot(i);
			if (!slot.getHasStack()) {
				targetSlot = i;
				break;
			}
		}

		if (targetSlot == -1)
			return;

		event.cancel();
		click(event, event.slotId);
		int finalTargetSlot = targetSlot;
		TickScheduler.runAfterClientTicks(() -> click(event, finalTargetSlot), requiresDelay(movedStack) ? 3 : 0);
	}

	private void click(WindowClickEvent event, int slot) {
		mc().playerController.windowClick(event.windowId, slot, 0, 0, player());
	}

	private boolean requiresDelay(ItemStack stack) {
		if (stack.getItem() == Items.filled_map)
			return true;

		return ItemUtil.getLore(stack).size() >= 3 && ItemUtil.getLoreAtIndex(stack, 0).equals("§r§7Du benötigst die neueste Version des Möbel-Addons");
	}

}
