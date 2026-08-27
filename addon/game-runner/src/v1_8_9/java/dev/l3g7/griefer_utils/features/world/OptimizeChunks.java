/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S22PacketMultiBlockChange.BlockUpdateData;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S24PacketBlockAction;
import net.minecraft.network.play.server.S28PacketEffect;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

@Singleton
public class OptimizeChunks extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Chunks optimieren")
		.description("Unterdrückt Änderungen an gängigen dynamischen Blöcken, wodurch Lags bei Farmen gefixt werden.",
			"§l§nDadurch können gewollte Änderungen versteckt werden.",
			"§l§nNur benutzen, wenn es wirklich benötigt ist!")
		.icon("measurement_circle_thingy")
		.since("2.4-BETA-1");

	@EventListener
	public void onMultiBlockChange(PacketReceiveEvent<S23PacketBlockChange> event) {
		if (world() == null)
			return;

		Block prev = world().getBlockState(event.packet.getBlockPosition()).getBlock();
		Block block = event.packet.getBlockState().getBlock();
		if (dropChange(prev) || dropChange(block))
			event.cancel();
	}

	@EventListener
	public void onBlockAction(PacketReceiveEvent<S24PacketBlockAction> event) {
		if (world() == null)
			return;

		Block prev = world().getBlockState(event.packet.getBlockPosition()).getBlock();
		if (prev != event.packet.getBlockType())
			event.cancel();
	}

	@EventListener
	public void onParticle(PacketReceiveEvent<S28PacketEffect> event) {
		if (event.packet.getSoundType() == 2001)
			event.cancel();
	}

	@EventListener
	public void onBlockChange(PacketReceiveEvent<S22PacketMultiBlockChange> event) {
		if (world() == null)
			return;

		var changes = event.packet.getChangedBlocks();
		boolean[] valid = new boolean[changes.length];
		for (int i = 0; i < changes.length; i++) {
			BlockUpdateData changedBlock = changes[i];
			Block prev = world().getBlockState(changedBlock.getPos()).getBlock();
			Block block = changedBlock.getBlockState().getBlock();
			valid[i] = !(dropChange(prev) || dropChange(block));
		}

		int validCount = 0;
		for (boolean flag : valid)
			if (flag)
				validCount++;

		if (validCount == changes.length)
			return;

		if (validCount == 0) {
			event.cancel();
			return;
		}

		BlockUpdateData[] newChanges = new BlockUpdateData[validCount];
		int idx = 0;
		for (int i = 0; i < changes.length; i++)
			if (valid[i])
				newChanges[idx++] = changes[i];

		Reflection.set(event.packet, "changedBlocks", newChanges);
	}

	private static boolean dropChange(Block block) {
		return block == Blocks.daylight_detector_inverted
			|| block == Blocks.unpowered_comparator
			|| block == Blocks.daylight_detector
			|| block == Blocks.unpowered_repeater
			|| block == Blocks.powered_repeater
			|| block == Blocks.redstone_wire
			|| block == Blocks.stone_pressure_plate
			|| block == Blocks.tripwire
			|| block == Blocks.water
			|| block == Blocks.flowing_water
			|| block == Blocks.lava
			|| block == Blocks.flowing_lava
			|| block == Blocks.piston
			|| block == Blocks.piston_extension
			|| block == Blocks.piston_head
			|| block == Blocks.sticky_piston
			|| block == Blocks.redstone_torch
			|| block == Blocks.unlit_redstone_torch
			|| block == Blocks.sapling
			|| block == Blocks.reeds
			|| block == Blocks.cactus
			|| block == Blocks.grass
			|| block == Blocks.dirt
			|| block == Blocks.red_flower
			|| block == Blocks.yellow_flower
			|| block == Blocks.tallgrass
			|| block == Blocks.deadbush
			;
	}

}
