/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render.render_engine;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.ChunkFilledEvent;
import dev.l3g7.griefer_utils.core.events.ChunkUnloadEvent;
import dev.l3g7.griefer_utils.core.events.WorldUnloadEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

/**
 * Generates {@link RenderChunk}s and updates their data
 */
class RenderChunkManager {

	static final Map<ChunkCoordIntPair, RenderChunk> chunks = new ConcurrentHashMap<>();

	@EventListener
	private static void onChunkFilled(ChunkFilledEvent event) {
		ChunkCoordIntPair coords = new ChunkCoordIntPair(event.getChunk().xPosition, event.getChunk().zPosition);
		RenderChunk chunk = new RenderChunk();

		for (ExtendedBlockStorage ebs : event.getChunk().getBlockStorageArray()) {
			if (ebs == null)
				continue;

			for (int x = 0; x < 16; x++) {
				for (int y = 0; y < 16; y++) {
					for (int z = 0; z < 16; z++) {
						BlockPos targetPos = new BlockPos(
							x + (coords.chunkXPos << 4),
							y + ebs.getYLocation(),
							z + (coords.chunkZPos << 4)
						);

						RenderObject renderObject = RenderObject.fromState(targetPos, ebs.get(x, y, z), world());
						if (renderObject == null)
							continue;

						chunk.add(new BlockPos(x, y + ebs.getYLocation(), z), renderObject);
					}
				}
			}
		}

		if (chunk.hasData())
			delete(chunks.put(coords, chunk));
		else
			delete(chunks.remove(coords));
	}

	@EventListener
	private static void onChunkUnload(ChunkUnloadEvent event) {
		if (!event.chunk.getWorld().isRemote)
			return; // Integrated server chunk, its thread has no GL context

		ChunkCoordIntPair coordPair = new ChunkCoordIntPair(event.chunk.xPosition, event.chunk.zPosition);
		delete(chunks.remove(coordPair));
	}

	@EventListener
	private static void onWorldUnload(WorldUnloadEvent event) {
		RenderChunk.deleteAll(chunks.values());
	}

	@EventListener
	private static void onPacket(PacketEvent.PacketReceiveEvent<Packet<?>> event) {
		if (event.packet instanceof S23PacketBlockChange packet) {
			TickScheduler.sync(() -> updateBlock(packet.getBlockPosition(), packet.getBlockState()));
			return;
		}

		if (!(event.packet instanceof S22PacketMultiBlockChange packet))
			return;

		TickScheduler.sync(() -> {
			for (S22PacketMultiBlockChange.BlockUpdateData data : packet.getChangedBlocks())
				updateBlock(data.getPos(), data.getBlockState());
		});
	}

	private static void delete(RenderChunk chunk) {
		if (chunk != null)
			chunk.delete();
	}

	private static void updateBlock(BlockPos pos, IBlockState state) {
		ChunkCoordIntPair pair = new ChunkCoordIntPair(pos.getX() >> 4, pos.getZ() >> 4);
		RenderObject renderObject = RenderObject.fromState(pos, state, world());
		BlockPos chunkPos = new BlockPos(pos.getX() & 15, pos.getY(), pos.getZ() & 16);

		if (renderObject != null) {
			RenderChunk chunk = chunks.computeIfAbsent(pair, k -> new RenderChunk());
			chunk.add(chunkPos, renderObject);
			return;
		}

		RenderChunk chunk = chunks.get(pair);
		if (chunk != null && chunk.hasData())
			chunk.remove(chunkPos);
	}


}
