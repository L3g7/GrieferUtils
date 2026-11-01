/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render.block_overlay;

import com.github.lunatrius.schematica.api.ISchematic;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.events.ChunkFilledEvent;
import dev.l3g7.griefer_utils.core.events.ChunkUnloadEvent;
import dev.l3g7.griefer_utils.core.events.WorldUnloadEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent;
import dev.l3g7.griefer_utils.core.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.misc.Vec3d;
import dev.l3g7.griefer_utils.core.util.SchematicaUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class BlockOverlayCollector {

	private static final Map<ChunkCoordIntPair, Map<BlockPos, RenderObject>> renderObjects = new ConcurrentHashMap<>();
	private static final Map<BlockPos, RenderObject> schematicasROs = new ConcurrentHashMap<>();
	private static final List<RenderObjectGenerator> generators = new ArrayList<>();

	private static Object previousSchematic = null;
	private static BlockPos previousSchematicPos = null;

	public static Map<BlockPos, RenderObject> getRenderObjectsForChunk(ChunkCoordIntPair pair) {
		return renderObjects.computeIfAbsent(pair, k -> new ConcurrentHashMap<>());
	}

	public static void registerRenderObjectGenerator(RenderObjectGenerator generator) {
		generators.add(generator);
	}

	public static void regenerate() {
		mc().renderGlobal.loadRenderers();
	}

	@EventListener
	private static void onChunkFilled(ChunkFilledEvent event) {
		TickScheduler.sync(() -> {
			Map<BlockPos, RenderObject> newRenderObjects = new ConcurrentHashMap<>();
			ChunkCoordIntPair coords = new ChunkCoordIntPair(event.getChunk().xPosition, event.getChunk().zPosition);

			for (ExtendedBlockStorage ebs : event.getChunk().getBlockStorageArray()) {
				if (ebs == null)
					continue;

				for (int x = 0; x < 16; x++) {
					for (int y = 0; y < 16; y++) {
						for (int z = 0; z < 16; z++) {
							BlockPos targetPos = coords.getBlock(x, y + ebs.getYLocation(), z);
							RenderObject redstoneRenderObject = RenderObject.fromState(ebs.get(x, y, z), targetPos, world());
							if (redstoneRenderObject == null)
								continue;

							newRenderObjects.put(targetPos, redstoneRenderObject);
						}
					}
				}
			}

			if (!newRenderObjects.isEmpty())
				renderObjects.put(coords, newRenderObjects);
			else
				renderObjects.remove(coords);
		});
	}

	@EventListener
	private static void onChunkUnload(ChunkUnloadEvent event) {
		ChunkCoordIntPair coordPair = new ChunkCoordIntPair(event.chunk.xPosition, event.chunk.zPosition);
		renderObjects.remove(coordPair);
	}

	@EventListener
	private static void onWorldUnload(WorldUnloadEvent event) {
		renderObjects.clear();
	}

	@EventListener
	private static void onPacket(PacketEvent.PacketReceiveEvent<Packet<?>> event) {
		if (event.packet instanceof S23PacketBlockChange packet) {
			TickScheduler.sync(() -> onBlockUpdate(packet.getBlockPosition(), packet.getBlockState()));
			return;
		}

		if (!(event.packet instanceof S22PacketMultiBlockChange packet))
			return;

		TickScheduler.sync(() -> {
			for (S22PacketMultiBlockChange.BlockUpdateData data : packet.getChangedBlocks())
				onBlockUpdate(data.getPos(), data.getBlockState());
		});
	}

	private static void onBlockUpdate(BlockPos pos, IBlockState state) {
		RenderObject redstoneRenderObject = RenderObject.fromState(state, pos, world());

		ChunkCoordIntPair pair = new ChunkCoordIntPair(pos.getX() >> 4, pos.getZ() >> 4);

		if (redstoneRenderObject != null) {
			RenderObject ro = RenderObject.fromState(state, pos, world());
			Map<BlockPos, RenderObject> map = getRenderObjectsForChunk(pair);

			if (ro == null)
				map.remove(pos);
			else
				map.put(pos, ro);
			return;
		}

		Map<BlockPos, RenderObject> map = renderObjects.get(pair);
		if (map == null)
			return;

		if (map.isEmpty())
			renderObjects.remove(pair);
	}

	@EventListener
	private static void onRenderWorldLast(RenderWorldLastEvent event) {
		if (renderObjects.isEmpty() && schematicasROs.isEmpty())
			return;

		mc().mcProfiler.startSection("blockoverlays");
		if (Constants.SCHEMATICA)
			updateSchematic(); // TODO: this

		GlStateManager.disableDepth();
		GlStateManager.disableCull();
		GlStateManager.disableTexture2D();

		for (Map.Entry<ChunkCoordIntPair, Map<BlockPos, RenderObject>> entry : renderObjects.entrySet())
			for (Map.Entry<BlockPos, RenderObject> chunkEntry : entry.getValue().entrySet())
				chunkEntry.getValue().render(chunkEntry.getKey(), event.partialTicks);

		if (Constants.SCHEMATICA)
			renderSchematicasRROs(event.partialTicks);

		GlStateManager.disableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.enableDepth();
		GlStateManager.enableCull();

		mc().mcProfiler.endSection();
	}

	private static void renderSchematicasRROs(float partialTicks) {
		if (SchematicaUtil.dontRender())
			return;

		for (Map.Entry<BlockPos, RenderObject> entry : schematicasROs.entrySet())
			if (SchematicaUtil.shouldLayerBeRendered(entry.getKey().getY()))
				entry.getValue().render(entry.getKey(), partialTicks);
	}

	private static void updateSchematic() {
		ISchematic schematic = SchematicaUtil.getWorld() == null ? null : SchematicaUtil.getSchematic();
		BlockPos position = SchematicaUtil.getWorld() == null ? null : SchematicaUtil.getPosition();

		if (previousSchematic == schematic && Objects.equals(previousSchematicPos, position))
			return;

		schematicasROs.clear();
		previousSchematic = schematic;
		previousSchematicPos = position == null ? null : position.up().down();

		if (schematic == null || position == null)
			return;

		for (int dX = 0; dX < schematic.getWidth(); dX++) {
			for (int dY = 0; dY < schematic.getHeight(); dY++) {
				for (int dZ = 0; dZ < schematic.getLength(); dZ++) {
					BlockPos pos = new BlockPos(dX, dY, dZ);
					RenderObject renderObject = RenderObject.fromState(schematic.getBlockState(pos), pos, SchematicaUtil.getWorld());
					if (renderObject == null)
						continue;

					schematicasROs.put(position.add(dX, dY, dZ), renderObject);
				}
			}
		}
	}

	public abstract static class RenderObject {

		private static RenderObject fromState(IBlockState state, BlockPos pos, WorldClient world) {
			if (world == null || state == null)
				return null;

			for (RenderObjectGenerator generator : generators) {
				RenderObject renderObject = generator.getRenderObject(state, pos, world);
				if (renderObject != null)
					return renderObject;
			}

			return null;
		}

		protected static void prepareRender(Vec3d loc, float partialTicks) {
			GlStateManager.pushMatrix();
			Entity viewer = mc().getRenderViewEntity();
			double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
			double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
			double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;
			double tx = (int) loc.x - viewerX + 0.5;
			double ty = loc.y - viewerY;
			double tz = (int) loc.z - viewerZ + 0.5;
			GlStateManager.translate(tx, ty, tz);

			GlStateManager.enableDepth();
			GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
			GlStateManager.enableTexture2D();
		}

		protected abstract void render(BlockPos pos, float partialTicks);

	}

	public interface RenderObjectGenerator {

		RenderObject getRenderObject(IBlockState state, BlockPos pos, WorldClient world);

	}

}
