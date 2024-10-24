/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.EggImpactEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

@Singleton
public class JailBarriers extends Feature {

	private static final BlockPos LEAVES_POS = new BlockPos(255, 37, 115);
	private Block targetBlock = Blocks.barrier;
	private Block placedBlock = Blocks.air;
	private int armorStandId = -1;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Jail-Barrieren")
		.icon(Blocks.iron_bars)
		.description("Fügt beim Jail Projektil-durchlässige Barrieren hinzu, um das Reinfallen zu verhindern.")
		.callback(b -> {
			targetBlock = b ? Blocks.barrier: Blocks.air;
			checkIfChunksAreLoaded();
		});

	@EventListener
	private void onCBSwitch(ServerSwitchEvent event) {
		placedBlock = Blocks.air;
	}

	@EventListener
	private void onPacket(PacketEvent.PacketReceiveEvent<Packet<?>> event) {
		if (event.packet instanceof S21PacketChunkData)
			checkIfChunksAreLoaded();

		if (event.packet instanceof S26PacketMapChunkBulk)
			checkIfChunksAreLoaded();

		// Detect going away from the jail
		if (event.packet instanceof S13PacketDestroyEntities) {
			S13PacketDestroyEntities packet = (S13PacketDestroyEntities) event.packet;
			for (int entityID : packet.getEntityIDs()) {
				if (armorStandId == entityID) {
					armorStandId = -1;
					placedBlock = Blocks.air;
					break;
				}
			}
		}

		// Detect going to the jail
		if (event.packet instanceof S0EPacketSpawnObject) {
			S0EPacketSpawnObject p = (S0EPacketSpawnObject) event.packet;
			// Check if it's a ArmorStand
			if (p.getType() != 78)
				return;

			// Check position
			if (p.getX() != 9807 || p.getY() != 544 || p.getZ() != 3676)
				return;

			// Check rotation
			if (p.getYaw() != -1 || p.getPitch() != 35)
				return;

			armorStandId = p.getEntityID();
			checkIfChunksAreLoaded();
		}
	}

	@EventListener
	private void onPacketSend(PacketEvent.PacketSendEvent<Packet<?>> event) {
		if (!isNearJail())
			return;

		if (event.packet instanceof C08PacketPlayerBlockPlacement) {
			C08PacketPlayerBlockPlacement packet = (C08PacketPlayerBlockPlacement) event.packet;
			if (world().getBlockState(packet.getPosition()).equals(Blocks.barrier.getDefaultState()))
				event.cancel();
		}

		if (event.packet instanceof C07PacketPlayerDigging) {
			C07PacketPlayerDigging packet = (C07PacketPlayerDigging) event.packet;
			if (world().getBlockState(packet.getPosition()).equals(Blocks.barrier.getDefaultState()))
				event.cancel();
		}
	}

	private void checkIfChunksAreLoaded() {
		if (!isNearJail() || placedBlock == targetBlock)
			return;

		TickScheduler.runAfterRenderTicks(() -> {
			if (world() == null)
				return;

			for (int dX = 0; dX < 3; dX++)
				for (int dY = 0; dY < 3; dY++)
					if (!world().getChunkFromChunkCoords(16 + dX, 5 + dY).isLoaded())
						return;

			if (!world().getChunkFromChunkCoords(19, 7).isLoaded() || !world().getChunkFromChunkCoords(19, 8).isLoaded())
				return;

			placeBarriers();
		}, 1);
	}

	private boolean isNearJail() {
		if (armorStandId != -1)
			return true;

		if (world() == null)
			return false;

		// Since the armor stand was unloaded when on the other side of the jail, grass on top of leaves is being checked for instead.
		if (world().getBlockState(LEAVES_POS).getBlock() != Blocks.leaves)
			return false;

		return world().getBlockState(LEAVES_POS.up()).getBlock() == Blocks.tallgrass;
	}

	@EventListener
	private void onEggImpact(EggImpactEvent event) {
		if (!isNearJail() || event.mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
			return;

		if (world().getBlockState(event.mop.getBlockPos()).getBlock() == Blocks.barrier)
			event.cancel();
	}

	private void placeBarriers() {
		// Windows
		column(297, 98);
		column(297, 102);

		// Main walls
		column(301, 90);
		column(300, 89);
		wall(299, 88, 295, 88);
		wall(295, 89, 295, 90);
		wall(294, 91, 288, 91);
		wall(287, 90, 287, 89);
		wall(287, 88, 285, 88);
		column(284, 87);
		wall(283, 88, 281, 88);
		wall(281, 89, 281, 90);
		wall(280, 91, 273, 91);
		wall(272, 90, 272, 89);
		wall(272, 88, 269, 88);
		column(268, 89);
		column(267, 90);
		column(266, 91);
		wall(265, 92, 265, 93);
		wall(266, 94, 266, 98);
		wall(265, 99, 265, 100);
		wall(266, 101, 266, 105);
		column(265, 106);

		// Middle barriers
		cuboid(265, 25, 107, 265, 32, 107);
		cuboid(294, 28, 105, 294, 31, 109);

		// Ceiling
		cuboid(266, 32, 91, 293, 32, 123);
		ceiling(268, 89, 271, 90);
		ceiling(282, 88, 286, 90);
		ceiling(294 , 92, 294, 103);
		ceiling(295 , 89, 296, 102);
		ceiling(297, 89, 297, 95);
		ceiling(298, 90, 298, 94);
		ceiling(299, 91, 299, 92);
		ceiling(300, 90, 300, 91);

		// Fix hole in the foundation
		cuboid(317, 16, 122, 317, 17, 134);
		placedBlock = targetBlock;
	}

	private void column(int x, int z) {
		wall(x, z, x, z);
	}

	private void wall(int x1, int z1, int x2, int z2) {
		int minX = Math.min(x1, x2);
		int maxX = Math.max(x1, x2);
		int minZ = Math.min(z1, z2);
		int maxZ = Math.max(z1, z2);

		cuboid(minX, 25, minZ, maxX, 32, maxZ);

		// Flip
		int flippedZ1 = 214 - minZ;
		int flippedZ2 = 214 - maxZ;
		cuboid(minX, 25, flippedZ2, maxX, 32, flippedZ1);
	}

	private void ceiling(int x1, int z1, int x2, int z2) {
		cuboid(x1, 32, z1, x2, 32, z2);
		cuboid(x1, 32, 214 - z2, x2, 32, 214 - z1);
	}

	private void cuboid(int x1, int y1, int z1, int x2, int y2, int z2) {
		for (int x = x1; x <= x2; x++)
			for (int y = y1; y <= y2; y++)
				for (int z = z1; z <= z2; z++)
					world().setBlockState(new BlockPos(x, y, z), targetBlock.getDefaultState(), 2);
	}

}
