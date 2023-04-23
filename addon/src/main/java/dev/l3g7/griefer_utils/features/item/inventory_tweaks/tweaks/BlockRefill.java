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
import dev.l3g7.griefer_utils.event.events.ItemUseEvent;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.item.inventory_tweaks.InventoryTweaks;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;

import java.util.Objects;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

@Singleton
public class BlockRefill extends InventoryTweaks.InventoryTweak {

	private ItemStack expectedStack = null;
	private int slot = 0;

	@MainElement
	private final BooleanSetting refillBlocks = new BooleanSetting()
		.name("Verbrauchte Blöcke nachziehen")
		.description("Ob Blöcke, die verbraucht wurden, mit gleichen aufgefüllt werden sollen")
		.icon(new ItemStack(Blocks.stone, 0));

	@EventListener
	public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
		if (expectedStack == null || player() == null)
			return;

		if (!(event.packet instanceof S2FPacketSetSlot))
			return;

		S2FPacketSetSlot packet = (S2FPacketSetSlot) event.packet;
		if (packet.func_149173_d() != slot)
			return;

		event.setCanceled(true);


		for (int slot = 0; slot < 36; slot++) {
			InventoryPlayer inventory = player().inventory;
			if (slot == inventory.currentItem)
				continue;

			ItemStack itemStack = inventory.getStackInSlot(slot);

			if (!expectedStack.isItemEqual(itemStack) || itemStack.stackSize <= 0)
				continue;

			// Compare damage
			if (!expectedStack.isItemStackDamageable() && expectedStack.getItemDamage() != itemStack.getItemDamage())
				continue;

			// Compare NBT
			if (!Objects.equals(expectedStack.getTagCompound(), itemStack.getTagCompound()))
				continue;

			ItemStack heldItem = player().getHeldItem();
			mc().playerController.windowClick(0, slot < 9 ? slot + 36 : slot, inventory.currentItem, 2, player());
			inventory.setInventorySlotContents(slot, (heldItem == null || heldItem.stackSize == 0) ? null : heldItem);
			inventory.setInventorySlotContents(inventory.currentItem, itemStack);
			break;
		}

		expectedStack = null;
	}

	@EventListener
	public void onItemUse(ItemUseEvent event) {
		if (!refillBlocks.get() || !(event.getStackBeforeUse().getItem() instanceof ItemBlock))
			return;

		if (!event.getStackBeforeUse().isItemEqual(event.getStackAfteruse())) {
			expectedStack = event.getStackBeforeUse();
			slot = player().inventory.currentItem + 36;
		}
	}

}
