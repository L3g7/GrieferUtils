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

package dev.l3g7.griefer_utils.features.item.item_saver;


import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.event.events.WindowClickEvent;
import dev.l3g7.griefer_utils.features.item.AutoTool;
import dev.l3g7.griefer_utils.features.item.item_info.info_suppliers.ItemCounter;
import dev.l3g7.griefer_utils.features.item.item_saver.ItemSaverCategory.ItemSaver;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.Arrays;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

@Singleton
public class OrbSaver extends ItemSaver {

	private static final int LIMIT = 4782969;
	private static final ItemStack compressedBlock;
	private static final ItemStack priceFellBlock;
	private static final ItemStack savedBlock;

	private final BooleanSetting onPriceFall = new BooleanSetting()
		.name("Bei Preisfall blockieren")
		.description("Deaktiviert Abgeben, wenn der Preis des Items gefallen ist.")
		.icon(ItemUtil.createItem(Blocks.gold_block, 0, true));

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Orb-Saver")
		.description("Deaktiviert Abgeben von zu vielen Items beim Orbhändler.")
		.icon(ItemUtil.createItem(Blocks.gold_block, 0, true))
		.subSettings(onPriceFall);

	@EventListener(triggerWhenDisabled = true)
	private void onWindowClick(WindowClickEvent event) {
		if (!(mc().currentScreen instanceof GuiChest))
			return;

		if (event.itemStack == null || !event.itemStack.hasTagCompound())
			return;

		if (!getGuiChestTitle().startsWith("§6Adventure-Jobs"))
			return;

		if (event.itemStack.getTagCompound().hasKey("compressionLevel")
			|| isInItemSaver(event.itemStack)
			|| event.itemStack.getDisplayName().equals("§c§lGeblockt!"))
			event.cancel();
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
		ItemStack lastBarItem = lowerChestInventory.getStackInSlot(44);

		ItemStack stack = null;
		if (onPriceFall.get() && lastBarItem != null && lastBarItem.getMetadata() == 5)
			stack = priceFellBlock;

		for (ItemStack itemStack : player().inventory.mainInventory) {
			if (sellingItem == null || stack != null)
				break;

			if (!sellingItem.isItemEqual(itemStack))
				continue;

			if (FileProvider.getSingleton(ItemCounter.class).getAmount(Arrays.asList(player().inventory.mainInventory), sellingItem) > LIMIT)
				stack = compressedBlock;
			else if (isInItemSaver(itemStack))
				stack = savedBlock;
			else
				continue;

			event.cancel();
			break;
		}

		if (stack != null) {
			((InventoryBasic) lowerChestInventory).setCustomName("§g§u§cGeblockt!");
			for (int i = 0; i < lowerChestInventory.getSizeInventory(); i++)
				if (i != 45)
					lowerChestInventory.setInventorySlotContents(i, stack);
		}

		if (title.equals("§g§u§cGeblockt!§r")) {
			Slot slot = getSlotUnderMouse(event.gui);
			if (slot != null && getSlotIndex(slot) != 45)
				event.cancel();
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
				if (!sellingItem.isItemEqual(itemStack) || AutoTool.isTool(itemStack) || !isInItemSaver(itemStack))
					continue;

				lowerChestInventory.setInventorySlotContents(i, savedBlock);
				event.cancel();
				break;
			}
		}

		if (getStackUnderMouse(event.gui) == compressedBlock)
			event.cancel();
	}

	private boolean isInItemSaver(ItemStack stack) {
		return dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver.ItemSaver.getSetting(stack) != null;
	}

	static {
		compressedBlock = ItemUtil.createItem(Blocks.stained_glass_pane, 14, "§c§lGeblockt!");
		priceFellBlock = ItemUtil.createItem(Blocks.stained_glass_pane, 14, "§c§lGeblockt!");
		savedBlock = ItemUtil.createItem(Blocks.stained_glass_pane, 14, "§c§lGeblockt!");

		ItemUtil.setLore(compressedBlock, "§cDu hast zu viele Items im Inventar!");
		ItemUtil.setLore(priceFellBlock, "§cDer Preis ist gefallen!");
		ItemUtil.setLore(savedBlock, "§cEin Item im Inventar ist im Item-Saver!");
	}

}
