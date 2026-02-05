/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.botd;

import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.minecraft.entity.DataWatcher.WatchableObject;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.play.server.S2FPacketSetSlot;

import java.util.List;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

class SpecialRewardHandler {

	private static ItemStack lastStack = null;
	private static int lastItemId = -1;

	@EventListener
	private static void onSpawnObject(PacketReceiveEvent<Packet<?>> event) {
		if (event.packet instanceof S0EPacketSpawnObject packet) {
			if (packet.getType() == 2) // Item
				lastItemId = packet.getEntityID();
			return;
		}

		if (event.packet instanceof S1CPacketEntityMetadata packet) {
			if (packet.getEntityId() != lastItemId)
				return;

			List<WatchableObject> watchableObjects = packet.func_149376_c();
			if (watchableObjects == null)
				// List is null if empty
				return;

			for (WatchableObject wo : watchableObjects) {
				if (wo.getDataValueId() != 10 /* Item */ || wo.getObjectType() != 5 /* ItemStack */)
					continue;

				lastStack = (ItemStack) wo.getObject();
				return;
			}
			return;
		}

		if (event.packet instanceof S2FPacketSetSlot packet) {
			if (player() == null || packet.func_149175_c() != 0 || packet.func_149174_e() == null)
				return;

			lastStack = packet.func_149174_e().copy();
			ItemStack previousItemStack = player().inventoryContainer.getSlot(packet.func_149173_d()).getStack();
			if (lastStack.isItemEqual(previousItemStack))
				lastStack.stackSize -= previousItemStack.stackSize;
		}
	}

	public static Reward parseReward() {
		if (lastStack == null) {
			BugReporter.reportError(new Throwable("BOTD message received stack :("));
			return null;
		}

		if (ItemUtil.getLastLore(lastStack).equals("§7Block des Tages X"))
			return new Reward(Reward.RewardType.BLOCK, lastStack.stackSize);

		return new Reward(lastStack);
	}

}
