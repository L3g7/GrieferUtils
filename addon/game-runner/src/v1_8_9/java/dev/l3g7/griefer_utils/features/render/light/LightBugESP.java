/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.render.light;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.ChunkFilledEvent;
import dev.l3g7.griefer_utils.core.events.ChunkUnloadEvent;
import dev.l3g7.griefer_utils.core.events.WorldUnloadEvent;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.render.GeometryRenderUtil;
import dev.l3g7.griefer_utils.core.util.render.render_engine.ChunkCompiler;
import dev.l3g7.griefer_utils.core.util.render.render_engine.RenderObject;
import dev.l3g7.griefer_utils.core.util.render.render_engine.manager.WorldRenderChunkManager;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumSkyBlock;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static dev.l3g7.griefer_utils.core.settings.types.SwitchSetting.TriggerMode.TOGGLE;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

@Singleton
public class LightBugESP extends Feature implements RenderObject.Generator {

	private final Map<ChunkCoordIntPair, Set<BlockPos>> pendingChunks = new HashMap<>();

	private final SwitchSetting inBlocks = SwitchSetting.create()
		.name("Lichtbugs in Blöcken anzeigen")
		.description("Zeigt auch Lichtbugs an, die sich in Blöcken befinden.")
		.icon("light_bulb_glitch")
		.callback(() -> {
			pendingChunks.clear();
			WorldRenderChunkManager.reloadAll();
		});

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Lichtbugs anzeigen")
		.description("Zeigt Lichtbugs an, auch durch Wände.")
		.icon("light_bulb_glitch")
		.subSettings(inBlocks)
		.addHotkeySetting("das Anzeigen der Lichtbugs", TOGGLE)
		.callback(() -> {
			pendingChunks.clear();
			WorldRenderChunkManager.reloadAll();
		});

	@Override
	public RenderObject getRenderObject(BlockPos worldPos, IBlockState state, WorldClient world) {
		ChunkCoordIntPair chunkCoord = new ChunkCoordIntPair(worldPos.getX() >> 4, worldPos.getZ() >> 4);

		Boolean result = processPosition(worldPos);
		if (Boolean.TRUE.equals(result))
			return LightBug.INSTANCE;

		if (result == null)
			pendingChunks.computeIfAbsent(chunkCoord, k -> new HashSet<>()).add(worldPos);
		return null;
	}

	@EventListener
	private void onChunkLoad(ChunkFilledEvent event) {
		TickScheduler.sync(() -> {
			int cX = event.getChunk().xPosition;
			int cZ = event.getChunk().zPosition;

			for (int[] delta : new int[][] {
				new int[] {  0,  1 },
				new int[] {  0, -1 },
				new int[] {  1,  0 },
				new int[] { -1,  0 },
			}) {
				ChunkCoordIntPair chunkCoord = new ChunkCoordIntPair(cX + delta[0], cZ + delta[1]);

				Set<BlockPos> positions = pendingChunks.get(chunkCoord);
				if (positions == null)
					continue;

				positions.removeIf(pos -> {
					Boolean result = processPosition(pos);
					if (Boolean.TRUE.equals(result))
						WorldRenderChunkManager.get().setRenderObject(pos, LightBug.INSTANCE);

					return result != null;
				});
				if (positions.isEmpty())
					pendingChunks.remove(chunkCoord);
			}
		});
	}

	@EventListener
	private void onWorldUnload(WorldUnloadEvent event) {
		pendingChunks.clear();
	}

	@EventListener
	private void onChunkUnload(ChunkUnloadEvent event) {
		ChunkCoordIntPair chunkCoord = new ChunkCoordIntPair(event.chunk.xPosition, event.chunk.zPosition);
		pendingChunks.remove(chunkCoord);
	}

	/**
	 * @return Whether the position has a light bug, or <code>null</code> if it couldn't be checked.
	 */
	private Boolean processPosition(BlockPos worldPos) {
		Block block = world().getBlockState(worldPos).getBlock();
		if (!inBlocks.get() && block != Blocks.air)
			return null;

		int light = world().getLightFor(EnumSkyBlock.BLOCK, worldPos);
		if (block.getLightValue() == light)
			return null;

		// Check if no neighbor has a higher light level (-> the light source is there)
		for (EnumFacing facing : EnumFacing.values()) {
			BlockPos neighbor = worldPos.offset(facing);
			if (!world().isBlockLoaded(neighbor, false))
				return null;

			if (world().getLightFor(EnumSkyBlock.BLOCK, neighbor) > light)
				return false;
		}

		// Check if block is visible
		if (!inBlocks.get())
			return true;

		boolean visible = false;
		for (EnumFacing facing : EnumFacing.values()) {
			if (block.shouldSideBeRendered(world(), worldPos.offset(facing), facing)) {
				visible = true;
				break;
			}
		}

		return visible;

	}

	private static class LightBug implements RenderObject {

		public static final LightBug INSTANCE = new LightBug();

		@Override
		public boolean equals(RenderObject r) {
			return r == INSTANCE;
		}

		@Override
		public void draw(ChunkCompiler compiler, BlockPos chunkPosition, int rotation) {
			compiler.disable(GL_DEPTH_TEST, GL_TEXTURE_2D);
			compiler.setColor(new Color(0x30FFFF00, true));
			GeometryRenderUtil.drawFilledBoxWhenRenderingStarted(compiler, new AxisAlignedBB(chunkPosition, chunkPosition.add(1, 1, 1)), false);
			compiler.enable(GL_DEPTH_TEST, GL_TEXTURE_2D);
		}
	}

}
