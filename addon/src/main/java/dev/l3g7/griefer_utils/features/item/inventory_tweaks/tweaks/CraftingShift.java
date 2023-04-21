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

package dev.l3g7.griefer_utils.features.item.inventory_tweaks.tweaks;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.WindowClickEvent;
import dev.l3g7.griefer_utils.features.item.inventory_tweaks.InventoryTweaks;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.utils.Material;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.inventory.Slot;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

@Singleton
public class CraftingShift extends InventoryTweaks.InventoryTweak {

	@MainElement
	private final BooleanSetting craftingShift = new BooleanSetting()
		.name("Ins Crafting Feld shiften")
		.description("Ermöglicht das Verschieben von Items mit \"Shift + Klick\" direkt in die Crafting-Eingabe")
		.icon(Material.WORKBENCH);

	@EventListener
	public void onWindowClick(WindowClickEvent event) {
		if (!craftingShift.get() || !(mc().currentScreen instanceof GuiCrafting))
			return;

		if (event.mode != 1 || event.slotId <= 9)
			return;

		GuiCrafting screen = (GuiCrafting) mc().currentScreen;
		event.setCanceled(true);

		int targetSlot = 0;
		for (int i = 1; i < 10; i++) {
			Slot slot = screen.inventorySlots.getSlot(i);
			if (!slot.getHasStack()) {
				targetSlot = i;
				break;
			}
		}

		if (targetSlot == 0)
			return;

		mc().playerController.windowClick(event.windowId, event.slotId, 0, 0, player());
		mc().playerController.windowClick(event.windowId, targetSlot, 0, 0, player());
	}

}
