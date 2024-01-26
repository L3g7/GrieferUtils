/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.v1_8_9.features.item.inventory_tweaks.tweaks;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.ItemUseEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.item.inventory_tweaks.InventoryTweaks;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2FPacketSetSlot;

import java.util.Objects;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.player;

@Singleton
public class BlockRefill extends InventoryTweaks.InventoryTweak {

	private ItemStack expectedStack = null;
	private int slot = 0;

	@MainElement
	private final SwitchSetting refillBlocks = SwitchSetting.create()
		.name("Verbrauchte Blöcke nachziehen")
		.description("Füllt Blöcke, die verbraucht wurden, mit gleichen auf.")
		.icon(new ItemStack(Blocks.stone, 0));

	@EventListener
	public void onPacketReceive(PacketEvent.PacketReceiveEvent<S2FPacketSetSlot> event) {
		if (expectedStack == null || player() == null)
			return;

		if (event.packet.func_149173_d() != slot)
			return;

		event.cancel();

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

			if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("stackSize"))
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
	public void onItemUse(ItemUseEvent.Post event) {
		if (!refillBlocks.get() || !(event.stackBeforeUse.getItem() instanceof ItemBlock
			|| (event.stackBeforeUse.getItem() == Items.dye && EnumDyeColor.byDyeDamage(event.stackBeforeUse.getMetadata()) == EnumDyeColor.BROWN)
			|| event.stackBeforeUse.getItem() == Items.redstone))
			return;

		ItemStack previousStack = event.stackBeforeUse;
		NBTTagCompound tag = previousStack.getTagCompound();
		if (tag != null && tag.hasKey("currentAmount") && tag.getInteger("currentAmount") != 1)
			return;

		if (!event.stackBeforeUse.isItemEqual(event.stackAfterUse)) {
			expectedStack = event.stackBeforeUse;
			slot = player().inventory.currentItem + 36;
		}
	}

}
