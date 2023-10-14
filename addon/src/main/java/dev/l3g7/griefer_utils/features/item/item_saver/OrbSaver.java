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
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.GuiModifyItemsEvent;
import dev.l3g7.griefer_utils.event.events.WindowClickEvent;
import dev.l3g7.griefer_utils.features.item.item_saver.ItemSaverCategory.ItemSaver;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

@Singleton
public class OrbSaver extends ItemSaver {

	private static final ItemStack priceFellBlock;
	private boolean clicking = false;

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Orb-Saver")
		.description("Deaktiviert Abgeben von Items beim Orbhändler, wenn der Preis des Items gefallen ist.")
		.icon(ItemUtil.createItem(Blocks.gold_block, 0, true));

	@EventListener
	private void onGuiSetItems(GuiModifyItemsEvent event) {
		if (!event.getTitle().startsWith("§6Orbs - Verkauf "))
			return;

		ItemStack lastBarItem = event.getItem(44);
		if (lastBarItem == null || lastBarItem.getMetadata() != 5)
			return;

		for (int i = 0; i < 54; i++)
			if (i != 45)
				event.setItem(i, priceFellBlock);
	}

	@EventListener
	private void onWindowClick(WindowClickEvent event) {
		if (event.itemStack != priceFellBlock || clicking)
			return;

		event.cancel();

		if (event.mode == 3) {
			clicking = true;
			mc().playerController.windowClick(event.windowId, 15, 0, 0, player());
			clicking = false;
		}
	}

	static {
		priceFellBlock = ItemUtil.createItem(Blocks.stained_glass_pane, 14, "§c§lGeblockt!");
		ItemUtil.setLore(priceFellBlock, "§cDer Preis ist gefallen!\n§7Klicke mit dem Mausrad, um die Items trotzdem abzugeben.");
	}

}
