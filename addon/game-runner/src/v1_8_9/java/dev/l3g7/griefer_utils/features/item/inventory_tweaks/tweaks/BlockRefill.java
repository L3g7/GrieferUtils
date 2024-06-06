/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.inventory_tweaks.tweaks;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.ItemUseEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.item.inventory_tweaks.InventoryTweaks;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2FPacketSetSlot;

import java.util.Objects;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

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

			int finalSlot = slot;
			TickScheduler.runAfterClientTicks(() -> {
				ItemStack heldItem = player().getHeldItem();
				mc().playerController.windowClick(0, finalSlot < 9 ? finalSlot + 36 : finalSlot, inventory.currentItem, 2, player());
				inventory.setInventorySlotContents(finalSlot, (heldItem == null || heldItem.stackSize == 0) ? null : heldItem);
				inventory.setInventorySlotContents(inventory.currentItem, itemStack);
			}, expectedStack.getItem() instanceof ItemBlock ? 0 : 1);
			break;
		}

		expectedStack = null;
	}

	@EventListener
	public void onItemUse(ItemUseEvent.Post event) {
		Item item = event.stackBeforeUse.getItem();

		if (!refillBlocks.get() || !(item instanceof ItemBlock
			|| (item == Items.dye && EnumDyeColor.byDyeDamage(event.stackBeforeUse.getMetadata()) == EnumDyeColor.BROWN)
			|| item == Items.redstone
			|| item instanceof ItemReed
			|| item instanceof ItemBucket))
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
