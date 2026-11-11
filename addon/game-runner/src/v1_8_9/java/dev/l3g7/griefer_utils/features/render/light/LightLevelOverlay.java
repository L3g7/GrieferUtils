/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.render.light;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.render.render_engine.ChunkCompiler;
import dev.l3g7.griefer_utils.core.util.render.render_engine.RenderChunk;
import dev.l3g7.griefer_utils.core.util.render.render_engine.RenderObject;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.Quad;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.text.TextGeometry;
import dev.l3g7.griefer_utils.core.util.render.render_engine.manager.RenderChunkManager;
import dev.l3g7.griefer_utils.core.util.render.render_engine.manager.WorldRenderChunkManager;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static dev.l3g7.griefer_utils.core.settings.types.SwitchSetting.TriggerMode.TOGGLE;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

@Singleton
public class LightLevelOverlay extends Feature {

	private static final LightLevelOverlayRenderChunkManager renderChunkManager = new LightLevelOverlayRenderChunkManager();

	@Override
	public void init() {
		super.init();
		RenderChunkManager.managers.add(renderChunkManager);
		EventRegisterer.register(renderChunkManager);
	}

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Lichtlevel anzeigen")
		.description("Zeigt das Lichtlevel auf Blöcken an.")
		.icon("light_bulb")
		.addHotkeySetting("das Anzeigen des Lichtlevels", TOGGLE);

	private record LightLevel(int level) implements RenderObject {

		@Override
		public boolean rotates() {
			return true;
		}

		@Override
		public boolean equals(RenderObject r) {
			return r instanceof LightLevel(int otherLevel) && this.level == otherLevel;
		}

		@Override
		public void draw(ChunkCompiler compiler, BlockPos chunkPosition, int rotation) {
			compiler.setColor(new Color(1f, level / 15f, 0f));

			for (TextGeometry.GlyphModel renderInfo : TextGeometry.getGlyphModels(String.valueOf(level))) {
				Quad offsets = renderInfo.rotatedVertices()[rotation];
				compiler.draw(renderInfo.texture(), offsets.translate(0, -0.485, 0));
			}
		}
	}

	@EventListener
	private void onBlockChange(PacketEvent.PacketReceiveEvent<S23PacketBlockChange> event) {
		renderChunkManager.registerForRescan(event.packet.getBlockPosition());
		renderChunkManager.submitRescan();
	}

	@EventListener
	private void onMultiBlockChange(PacketEvent.PacketReceiveEvent<S22PacketMultiBlockChange> event) {
		for (S22PacketMultiBlockChange.BlockUpdateData changedBlock : event.packet.getChangedBlocks())
			renderChunkManager.registerForRescan(changedBlock.getPos());
		renderChunkManager.submitRescan();
	}

	private static class LightLevelOverlayRenderChunkManager extends WorldRenderChunkManager {

		private final Set<ChunkCoordIntPair> chunksToRescan = new HashSet<>();

		/**
		 * Saves the position's chunk and its neighboring chunks to rescan.
		 */
		private void registerForRescan(BlockPos pos) {
			int chunkX = pos.getX() >> 4;
			int chunkZ = pos.getZ() >> 4;

			synchronized (chunksToRescan) {
				for (int dZ = -1; dZ <= 1; dZ++)
					for (int dX = -1; dX <= 1; dX++)
						chunksToRescan.add(new ChunkCoordIntPair(chunkX + dX, chunkZ + dZ));
			}
		}

		/**
		 * Submits a rescan to run on the next tick.
		 */
		private void submitRescan() {
			TickScheduler.runNextClientTick(() -> {
				WorldClient world = world();
				if (world == null)
					return;

				synchronized (chunksToRescan) {
					IChunkProvider chunkProvider = world.getChunkProvider();

					for (ChunkCoordIntPair coord : chunksToRescan) {
						RenderChunk renderChunk = chunks.remove(coord);
						if (renderChunk != null)
							renderChunk.delete();

						if (!chunkProvider.chunkExists(coord.chunkXPos, coord.chunkZPos))
							continue;

						Chunk chunk = chunkProvider.provideChunk(coord.chunkXPos, coord.chunkZPos);
						if (!chunk.isEmpty())
							scanChunk(chunk);
					}

					chunksToRescan.clear();
				}
			});
		}

		@Override
		protected RenderObject fromState(BlockPos worldPos, IBlockState state, WorldClient world) {
			if (!FileProvider.getSingleton(LightLevelOverlay.class).isEnabled())
				return null;

			if (world().getBlockState(worldPos).getBlock() != Blocks.air)
				return null;

			Block downBlock = world().getBlockState(worldPos.down()).getBlock();
			if (!isUpperSideSolid(downBlock, world(), worldPos.down()) && !(downBlock instanceof BlockFarmland))
				return null;

			return new LightLevel(world().getLightFor(EnumSkyBlock.BLOCK, worldPos));
		}

	}

	private static boolean isUpperSideSolid(Block block, IBlockAccess world, BlockPos pos) {
		if (block instanceof BlockFarmland)
			return false;

		if (block instanceof BlockHopper || block instanceof BlockCompressedPowered)
			return true;

		IBlockState state = block.getActualState(world.getBlockState(pos), world, pos);

		if (block instanceof BlockSnow)
			return state.getValue(BlockSnow.LAYERS) >= 8;

		if (block instanceof BlockStairs)
			return state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP;

		if (block instanceof BlockSlab)
			return block.isFullBlock() || (state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP);

		return block.isNormalCube();
	}

}
