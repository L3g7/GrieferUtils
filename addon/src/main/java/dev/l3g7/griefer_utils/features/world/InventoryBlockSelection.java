/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.BlockPickEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

@Singleton
public class InventoryBlockSelection extends Feature {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Blockauswahl mit Inventar")
		.description("Erweitert die Blockauswahl um das Inventar.")
		.icon("middle_click");

	@EventListener
	public void onBlockPick(BlockPickEvent event) {
		// Check if it's not already in the hotbar
		for (int slot = 0; slot < 9; slot++)
			if (isStackInSlot(event.requiredStack, slot))
				return;

		InventoryPlayer inv = player().inventory;

		// If possible, the stack is put in an empty slot
		int targetSlot = inv.getFirstEmptyStack();
		if (targetSlot == -1 || targetSlot >= 9)
			targetSlot = inv.currentItem;

		for (int slot = 9; slot < 36; slot++) {
			if (isStackInSlot(event.requiredStack, slot)) {
				// Move stack
				mc().playerController.windowClick(0, slot, targetSlot, 2, player());

				// Switch selected slot
				if (targetSlot != inv.currentItem)
					inv.currentItem = targetSlot;

				event.setCanceled(true);
				return;
			}
		}
	}

	private static boolean isStackInSlot(ItemStack requiredStack, int slot) {
		ItemStack stack = player().inventory.getStackInSlot(slot);
		return stack != null && stack.isItemEqual(requiredStack) && ItemStack.areItemStackTagsEqual(stack, requiredStack);
	}


}
