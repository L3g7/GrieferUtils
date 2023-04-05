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
import dev.l3g7.griefer_utils.features.item.item_saver.ItemSaver;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.labymod.utils.Material;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

@Singleton
public class InventoryBlockSelection extends Feature {

	private final BooleanSetting compressed = new BooleanSetting()
		.name("Komprimierte Items")
		.description("Ob komprimierte Items bei der Blockauswahl auch ausgewählt werden sollen.")
		.icon(Material.STONE);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Blockauswahl mit Inventar")
		.description("Erweitert die Blockauswahl um das Inventar.")
		.icon("middle_click")
		.subSettings(compressed);

	@EventListener
	public void onBlockPick(BlockPickEvent event) {
		InventoryPlayer inv = player().inventory;

		// If possible, the stack is put in an empty slot
		int targetSlot = inv.getFirstEmptyStack();
		if (targetSlot == -1 || targetSlot >= 9)
			targetSlot = inv.currentItem;

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
			event.setCanceled(true);
			return;
		}

		// Move stack
		mc().playerController.windowClick(0, bestSlot, targetSlot, 2, player());

		if (inv.currentItem != targetSlot)
			inv.currentItem = targetSlot;

		event.setCanceled(true);
	}

	private int getScore(ItemStack requiredStack, int slot) {
		ItemStack stack = player().inventory.getStackInSlot(slot);
		if (stack == null || !stack.isItemEqual(requiredStack) || ItemSaver.getSetting(stack) != null)
			return -1;

		if (!compressed.get())
			return 0;

		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("stackSize"))
			return Integer.MAX_VALUE;

		if (!stack.getTagCompound().getBoolean("warningDisplayed"))
			return 0;

		String line = ItemUtil.getLore(stack).get(0);
		return Integer.parseInt(line.substring(12).replace(".", ""));
	}


}
