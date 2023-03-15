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

package dev.l3g7.griefer_utils.features.player;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.Citybuild;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.input.Mouse;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

@Singleton
public class InteractableProfiles extends Feature {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Interagierbare Profile")
		.description("Macht den Kopf eines Spielers in seinem Profil sowie die Citybuild-Anzeige interagierbar.")
		.icon("left_click");

	@EventListener
	public void onMouse(GuiScreenEvent.MouseInputEvent event) {
		if (!Mouse.getEventButtonState() || !(event.gui instanceof GuiChest))
			return;

		int button = Mouse.getEventButton();
		if (button != 0 && button != 1)
			return;

		GuiChest gui = (GuiChest) event.gui;
		if (gui.getSlotUnderMouse() == null)
			return;

		IInventory lowerChestInventory = Reflection.get(event.gui, "lowerChestInventory");
		String title = lowerChestInventory.getDisplayName().getFormattedText();
		if (!title.startsWith("§6Profil"))
			return;

		Slot slot = gui.inventorySlots.getSlot(13);
		if (slot == gui.getSlotUnderMouse()) {
			if (!slot.getHasStack())
				return;

			String clan = ItemUtil.getLastLore(slot.getStack()).substring(10);
			if (clan.equals("Kein Clan"))
				return;

			player().sendChatMessage("/clan info " + clan);
			mc().displayGuiScreen(null);
		}

		slot = gui.inventorySlots.getSlot(32);
		if (slot != gui.getSlotUnderMouse() || !slot.getHasStack())
			return;

		// CityBuild is not visible
		if (slot.getStack().getItem() == Item.getItemFromBlock(Blocks.barrier))
			return;

		String citybuild = ItemUtil.getLastLore(slot.getStack());
		citybuild = citybuild.substring(citybuild.lastIndexOf(' ') + 1).replaceAll("§.", "");
		citybuild = citybuild.substring(0, citybuild.length() - 1);

		Citybuild cb = Citybuild.getCitybuild(citybuild);

		if (!cb.isOnCb())
			cb.join();
	}

}