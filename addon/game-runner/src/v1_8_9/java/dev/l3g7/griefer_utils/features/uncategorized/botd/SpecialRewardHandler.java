/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.botd;

import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.minecraft.entity.DataWatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;

class SpecialRewardHandler {

	private static S1CPacketEntityMetadata lastS1C = null;
	private static int lastItemId = -1;

	@EventListener
	private static void onSpawnObject(PacketReceiveEvent<Packet<?>> p) {
		if (p.packet instanceof S0EPacketSpawnObject pso) {
			if (pso.getType() == 2)
				lastItemId = pso.getEntityID();
		} else if (p.packet instanceof S1CPacketEntityMetadata s1c) {
			if (s1c.getEntityId() == lastItemId)
				lastS1C = s1c;
		}
	}

	public static Reward parseReward() {
		for (DataWatcher.WatchableObject wo : lastS1C.func_149376_c()) {
			if (wo.getDataValueId() != 10)
				continue;

			ItemStack stack = (ItemStack) wo.getObject();
			if (ItemUtil.getLastLore(stack).equals("§7Block des Tages X"))
				return new Reward(Reward.RewardType.BLOCK, stack.stackSize);

			return new Reward(stack);
		}

		BugReporter.reportError(new Throwable("BOTD message w/o valid s1c :("));
		return null;
	}

}
