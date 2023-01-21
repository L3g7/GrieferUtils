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
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraftforge.client.event.MouseEvent;

import static dev.l3g7.griefer_utils.util.ItemUtil.createItem;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;
import static net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM;

/**
 * Suppresses left clicks and dropping when holding a diamond sword enchanted with looting 21.
 */
@Singleton
public class SwordSaver extends Feature {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Birth- / BonzeSaver")
		.description("Deaktiviert Linksklicks und Dropping bei Diamantschwertern mit Plünderung 21.")
		.icon(createItem(Items.diamond_sword, 0, true));

	/**
	 * Suppresses left clicks if the held item should be saved.
	 */
	@EventListener
	private void onMouse(MouseEvent event) {
		// Only disable left clicks
		if (event.button != 0 || !event.buttonstate)
			return;

		if (player() == null)
			return;

		InventoryPlayer inv = player().inventory;
		if (inv.getCurrentItem() == null)
			return;

		if (shouldSave(inv.getCurrentItem()))
			event.setCanceled(true);
	}

	/**
	 * Suppresses dropping if the dropped item should be saved.
	 */
	@EventListener
	private void onPacketSend(PacketEvent.PacketSendEvent event) {
		if (!(event.packet instanceof C07PacketPlayerDigging))
			return;

		C07PacketPlayerDigging.Action action = ((C07PacketPlayerDigging) event.packet).getStatus();

		// Prevent dropping
		if ((action != RELEASE_USE_ITEM) && shouldSave(player().getHeldItem()))
			event.setCanceled(true);
	}

	/**
	 * @return true if the item is a diamond sword and enchanted with looting 21.
	 */
	private boolean shouldSave(ItemStack itemStack) {
		if (itemStack == null)
			return false;

		if (itemStack.getItem() != Items.diamond_sword)
			return false;

		return EnchantmentHelper.getEnchantmentLevel(Enchantment.looting.effectId, itemStack) == 21;
	}

}
