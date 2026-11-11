/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render.render_engine.manager;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.ChunkFilledEvent;
import dev.l3g7.griefer_utils.core.events.ChunkUnloadEvent;
import dev.l3g7.griefer_utils.core.events.WorldUnloadEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.util.render.render_engine.RenderChunk;
import dev.l3g7.griefer_utils.core.util.render.render_engine.RenderObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

/**
 * The {@link RenderChunkManager} responsible for {@link RenderObject RenderObjects} in the world.
 * Handles blockstate changes and chunk loads / unloads.
 */
@Singleton
public class WorldRenderChunkManager extends RenderChunkManager {

	public static WorldRenderChunkManager get() {
		return FileProvider.getSingleton(WorldRenderChunkManager.class);
	}

	/**
	 * Rescans all loaded chunks.
	 */
	@Override
	public void rescan() {
		WorldClient world = world();
		if (world == null)
			return;

		RenderChunk.deleteAll(chunks.values());

		IChunkProvider chunkProvider = world.getChunkProvider();
		int playerX = player().chunkCoordX;
		int playerZ = player().chunkCoordZ;

		int range = mc().gameSettings.renderDistanceChunks;

		for (int dX = -range; dX <= range; dX++) {
			for (int dZ = -range; dZ <= range; dZ++) {
				if (!chunkProvider.chunkExists(playerX + dX, playerZ + dZ))
					continue;

				Chunk chunk = chunkProvider.provideChunk(playerX + dX, playerZ + dZ);
				if (!chunk.isEmpty())
					scanChunk(chunk);
			}
		}
	}

	@EventListener
	private void onChunkFilled(ChunkFilledEvent event) {
		scanChunk(event.getChunk());
	}

	/**
	 * Scans a single chunk for {@link RenderObject RenderObjects}.
	 * @param chunk The chunk to scan.
	 */
	protected void scanChunk(Chunk chunk) {
		ChunkCoordIntPair coords = new ChunkCoordIntPair(chunk.xPosition, chunk.zPosition);
		RenderChunk renderChunk = new RenderChunk();

		for (ExtendedBlockStorage ebs : chunk.getBlockStorageArray()) {
			if (ebs == null)
				continue;

			for (int x = 0; x < 16; x++) {
				for (int y = 0; y < 16; y++) {
					for (int z = 0; z < 16; z++) {
						IBlockState state = ebs.get(x, y, z);
						BlockPos targetPos = new BlockPos(
							x + (coords.chunkXPos << 4),
							y + ebs.getYLocation(),
							z + (coords.chunkZPos << 4)
						);

						RenderObject renderObject = fromState(targetPos, state, world());
						if (renderObject == null)
							continue;

						renderChunk.add(new BlockPos(x, y + ebs.getYLocation(), z), renderObject);
					}
				}
			}
		}

		RenderChunk oldChunk = renderChunk.hasData()
			? chunks.put(coords, renderChunk)
			: chunks.remove(coords);

		if (oldChunk != null)
			oldChunk.delete();
	}

	@EventListener
	private void onChunkUnload(ChunkUnloadEvent event) {
		if (!event.chunk.getWorld().isRemote)
			return; // Integrated server chunk, its thread has no GL context

		ChunkCoordIntPair coordPair = new ChunkCoordIntPair(event.chunk.xPosition, event.chunk.zPosition);
		RenderChunk oldChunk = chunks.remove(coordPair);
		if (oldChunk != null)
			oldChunk.delete();
	}

	@EventListener
	private void onWorldUnload(WorldUnloadEvent event) {
		RenderChunk.deleteAll(chunks.values());
	}

	@EventListener
	private void onBlockChange(PacketReceiveEvent<S23PacketBlockChange> event) {
		TickScheduler.sync(() -> {
			BlockPos pos = event.packet.getBlockPosition();
			RenderObject renderObject = fromState(pos, event.packet.getBlockState(), world());
			setRenderObject(pos, renderObject);
		});
	}

	@EventListener
	private void onMultiBlockChange(PacketReceiveEvent<S22PacketMultiBlockChange> event) {
		TickScheduler.sync(() -> {
			for (S22PacketMultiBlockChange.BlockUpdateData data : event.packet.getChangedBlocks()) {
				RenderObject renderObject = fromState(data.getPos(), data.getBlockState(), world());
				setRenderObject(data.getPos(), renderObject);
			}
		});
	}

	/**
	 * @param worldPos The RenderObject's position in the world.
	 * @param state The block state at the given position.
	 * @param world The world to use.
	 * @return The RenderObject for the given position, or <code>null</code> if there is none.
	 */
	protected RenderObject fromState(BlockPos worldPos, IBlockState state, WorldClient world) {
		return RenderObject.fromState(worldPos, state, world);
	}

}
