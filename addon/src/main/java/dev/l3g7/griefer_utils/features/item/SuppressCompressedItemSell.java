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

package dev.l3g7.griefer_utils.features.item;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.WindowClickEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.item.item_saver.ItemSaver;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

/**
 * Suppresses selling of compressed items.
 */
@Singleton
public class SuppressCompressedItemSell extends Feature {

	private static final ItemStack compressedBlock;
	private static final ItemStack savedBlock;

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Abgeben komprimierter Items deaktivieren")
		.description("Deaktiviert Orb-Verkäufe von komprimierten Items.")
		.icon(ItemUtil.createItem(Blocks.gold_block, 0, true));

	@EventListener(triggerWhenDisabled = true)
	public void onWindowClick(WindowClickEvent event) {
		if (!(mc().currentScreen instanceof GuiChest))
			return;

		if (event.itemStack == null || !event.itemStack.hasTagCompound())
			return;

		IInventory lowerChestInventory = Reflection.get(mc().currentScreen, "lowerChestInventory");
		String title = lowerChestInventory.getDisplayName().getFormattedText();
		if (!title.startsWith("§6Adventure-Jobs"))
			return;


		if ((isEnabled() && event.itemStack.getTagCompound().hasKey("compressionLevel"))
			|| ItemSaver.getSetting(event.itemStack) != null
			|| event.itemStack.getDisplayName().equals("§c§lGeblockt!"))
			event.setCanceled(true);
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMouseGui(GuiScreenEvent.MouseInputEvent.Pre event) {
		if (!(event.gui instanceof GuiChest))
			return;

		suppressOrbs(event);
		suppressJobs(event);
	}

	private void suppressOrbs(GuiScreenEvent.MouseInputEvent.Pre event) {
		IInventory lowerChestInventory = Reflection.get(event.gui, "lowerChestInventory");
		String title = lowerChestInventory.getDisplayName().getFormattedText();
		if (!title.startsWith("§6Orbs") && !title.equals("§g§u§cGeblockt!§r"))
			return;

		ItemStack sellingItem = lowerChestInventory.getStackInSlot(11);

		for (ItemStack itemStack : player().inventory.mainInventory) {
			if (sellingItem == null)
				break;

			if (!sellingItem.isItemEqual(itemStack))
				continue;

			ItemStack stack;
			if (isEnabled() && itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("compressionLevel"))
				stack = compressedBlock;
			else if (ItemSaver.getSetting(itemStack) != null)
				stack = savedBlock;
			else
				continue;

			((InventoryBasic) lowerChestInventory).setCustomName("§g§u§cGeblockt!");
			for (int i = 0; i < lowerChestInventory.getSizeInventory(); i++)
				if (i != 45)
					lowerChestInventory.setInventorySlotContents(i, stack);
			event.setCanceled(true);
			break;
		}

		if (title.equals("§g§u§cGeblockt!§r")) {
			Slot slot = ((GuiContainer) event.gui).getSlotUnderMouse();
			event.setCanceled(slot != null && slot.getSlotIndex() != 45);
		}
	}

	private void suppressJobs(GuiScreenEvent.MouseInputEvent.Pre event) {
		IInventory lowerChestInventory = Reflection.get(event.gui, "lowerChestInventory");
		String title = lowerChestInventory.getDisplayName().getFormattedText();
		if (!title.startsWith("§6Adventure-Jobs"))
			return;

		for (int i : new int[] {10, 13, 16}) {
			ItemStack sellingItem = lowerChestInventory.getStackInSlot(i);
			if (sellingItem == null)
				continue;

			for (ItemStack itemStack : player().inventory.mainInventory) {
				if (!sellingItem.isItemEqual(itemStack))
					continue;

				if (isEnabled() && itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("compressionLevel"))
					lowerChestInventory.setInventorySlotContents(i, compressedBlock);
				else if (ItemSaver.getSetting(itemStack) != null)
					lowerChestInventory.setInventorySlotContents(i, savedBlock);
				else
					continue;

				event.setCanceled(true);
				break;
			}
		}

		Slot slot = ((GuiContainer) event.gui).getSlotUnderMouse();
		event.setCanceled(slot != null && slot.getHasStack() && slot.getStack() == compressedBlock);
	}

	static {
		compressedBlock = ItemUtil.createItem(Blocks.stained_glass_pane, 14, "§c§lGeblockt!");
		ItemUtil.setLore(compressedBlock, "§cDu hast ein komprimiertes Item im Inventar!");
		savedBlock = ItemUtil.createItem(Blocks.stained_glass_pane, 14, "§c§lGeblockt!");
		ItemUtil.setLore(savedBlock, "§cEin Item im Inventar ist im ItemSaver!");
	}

}
