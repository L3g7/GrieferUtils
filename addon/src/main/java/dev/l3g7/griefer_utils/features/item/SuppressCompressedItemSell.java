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

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.WindowClickEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.util.Constants;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

/**
 * Suppresses selling of compressed items.
 */
@Singleton
public class SuppressCompressedItemSell extends Feature {

	private static final ItemStack blocked;

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Abgeben komprimierter Items deaktivieren")
		.description("Deaktiviert Orb-Verkäufe von komprimierten Items.")
		.icon(ItemUtil.createItem(Blocks.gold_block, 0, true));

	@EventListener
	public void onWindowClick(WindowClickEvent event) {
		if (!(mc().currentScreen instanceof GuiChest))
			return;

		if (event.itemStack == null || !event.itemStack.hasTagCompound())
			return;

		IInventory lowerChestInventory = Reflection.get(mc().currentScreen, "lowerChestInventory");
		String title = lowerChestInventory.getDisplayName().getFormattedText();
		if (!title.startsWith("§6Adventure-Jobs"))
			return;


		if (event.itemStack.getTagCompound().hasKey("compressionLevel"))
			event.setCanceled(true);
	}

	@EventListener
	public void onMouseGui(GuiScreenEvent.MouseInputEvent.Pre event) {
		if (!(event.gui instanceof GuiChest))
			return;

		suppressOrbs(event);
		suppressJobs(event);
	}

	private void suppressOrbs(GuiScreenEvent.MouseInputEvent.Pre event) {
		IInventory lowerChestInventory = Reflection.get(event.gui, "lowerChestInventory");
		String title = lowerChestInventory.getDisplayName().getFormattedText();
		if (!title.startsWith("§6Orbs") && !title.equals("§cGeblockt!§r"))
			return;

		ItemStack sellingItem = lowerChestInventory.getStackInSlot(11);

		boolean hasMatchingCompressedItem = false;
		for (ItemStack itemStack : player().inventory.mainInventory) {
			if (sellingItem == null) // Break here to so the canceling still happens
				break;

			if (itemStack == null || itemStack.getTagCompound() == null) continue;

			if (sellingItem.isItemEqual(itemStack) && itemStack.getTagCompound().hasKey("compressionLevel")) {
				hasMatchingCompressedItem = true;
				break;
			}
		}
		if (hasMatchingCompressedItem) {
			((InventoryBasic) lowerChestInventory).setCustomName("§cGeblockt!");
			for (int i = 0; i < lowerChestInventory.getSizeInventory(); i++) {
				if (i != 45)
					lowerChestInventory.setInventorySlotContents(i, blocked);
			}
			event.setCanceled(true);
		}

		if (title.equals("§cGeblockt!§r")) {
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

			boolean hasMatchingCompressedItem = false;
			for (ItemStack itemStack : player().inventory.mainInventory) {
				if (itemStack == null || itemStack.getTagCompound() == null) continue;

				if (sellingItem.isItemEqual(itemStack) && itemStack.getTagCompound().hasKey("compressionLevel")) {
					hasMatchingCompressedItem = true;
					break;
				}
			}

			if (hasMatchingCompressedItem) {
				lowerChestInventory.setInventorySlotContents(i, blocked);
				event.setCanceled(true);
			}

		}

		Slot slot = ((GuiContainer) event.gui).getSlotUnderMouse();
		event.setCanceled(slot != null && slot.getHasStack() && slot.getStack() == blocked);

	}

	static {
		blocked = ItemUtil.createItem(Blocks.stained_glass_pane, 14, "§c§lGeblockt!");
		NBTTagList l = blocked.getTagCompound().getCompoundTag("display").getTagList("Lore", Constants.NBT.TAG_STRING);
		l.appendTag(new NBTTagString("§cDu hast ein komprimiertes Item im Inventar!"));
		blocked.getTagCompound().getCompoundTag("display").setTag("Lore", l);
	}

}
