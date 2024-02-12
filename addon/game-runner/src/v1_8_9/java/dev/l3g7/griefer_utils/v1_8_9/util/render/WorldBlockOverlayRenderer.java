/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.util.render;

import com.github.lunatrius.schematica.api.ISchematic;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.v1_8_9.events.ChunkFilledEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.ChunkUnloadEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.v1_8_9.misc.Vec3d;
import dev.l3g7.griefer_utils.v1_8_9.util.SchematicaUtil;
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

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.*;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class WorldBlockOverlayRenderer {

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

	@EventListener
	private static void onChunkFilled(ChunkFilledEvent event) {
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
	}

	@EventListener
	private static void onChunkUnload(ChunkUnloadEvent event) {
		ChunkCoordIntPair coordPair = new ChunkCoordIntPair(event.chunk.xPosition, event.chunk.zPosition);
		renderObjects.remove(coordPair);
	}

	@EventListener
	private static void onServerChange(ServerEvent.ServerSwitchEvent event) {
		renderObjects.clear();
	}

	@EventListener
	private static void onPacket(PacketEvent.PacketReceiveEvent<Packet<?>> event) {
		if (event.packet instanceof S23PacketBlockChange packet) {
			onBlockUpdate(packet.getBlockPosition(), packet.getBlockState());
			return;
		}

		if (!(event.packet instanceof S22PacketMultiBlockChange packet))
			return;

		for (S22PacketMultiBlockChange.BlockUpdateData data : packet.getChangedBlocks())
			onBlockUpdate(data.getPos(), data.getBlockState());
	}

	private static void onBlockUpdate(BlockPos pos, IBlockState state) {
		RenderObject redstoneRenderObject = RenderObject.fromState(state, pos, world());

		ChunkCoordIntPair pair = new ChunkCoordIntPair(pos.getX() >> 4, pos.getZ() >> 4);

		if (redstoneRenderObject != null) {
			getRenderObjectsForChunk(pair).put(pos, RenderObject.fromState(state, pos, world()));
			return;
		}

		Map<BlockPos, RenderObject> map = renderObjects.get(pair);
		if (map == null)
			return;

		for (RenderObjectGenerator generator : generators)
			generator.onBlockUpdate(map, pos, state);

		if (map.isEmpty())
			renderObjects.remove(pair);
	}

	@EventListener
	private static void onRenderWorldLast(RenderWorldLastEvent event) {
		if (!isEnabled() || (renderObjects.isEmpty() && schematicasROs.isEmpty()))
			return;

		if (Constants.SCHEMATICA)
			updateSchematic();

		GlStateManager.disableDepth();
		GlStateManager.disableCull();
		GlStateManager.disableTexture2D();

		for (Map.Entry<ChunkCoordIntPair, Map<BlockPos, RenderObject>> entry : renderObjects.entrySet()) {
			int chunksFromPlayer = Math.max(Math.abs(entry.getKey().chunkXPos - player().chunkCoordX), Math.abs(entry.getKey().chunkZPos - player().chunkCoordZ));

			for (Map.Entry<BlockPos, RenderObject> chunkEntry : entry.getValue().entrySet())
				if (chunkEntry.getValue().generator.isEnabled())
					chunkEntry.getValue().render(chunkEntry.getKey(), event.partialTicks, chunksFromPlayer);
		}

		if (Constants.SCHEMATICA)
			renderSchematicasRROs(event.partialTicks);

		GlStateManager.disableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.enableDepth();
		GlStateManager.enableCull();
	}

	private static void renderSchematicasRROs(float partialTicks) {
		if (SchematicaUtil.dontRender())
			return;

		for (Map.Entry<BlockPos, RenderObject> entry : schematicasROs.entrySet())
			if (SchematicaUtil.shouldLayerBeRendered(entry.getKey().getY()))
				if (entry.getValue().generator.isEnabled())
					entry.getValue().render(entry.getKey(), partialTicks, 0);
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

	private static boolean isEnabled() {
		for (RenderObjectGenerator container : generators)
			if (container.isEnabled())
				return true;

		return false;
	}

	public abstract static class RenderObject {

		private static RenderObject fromState(IBlockState state, BlockPos pos, WorldClient world) {
			for (RenderObjectGenerator generator: generators) {
				RenderObject renderObject = generator.getRenderObject(state, pos, world);
				if (renderObject != null)
					return renderObject;
			}

			return null;
		}

		private final RenderObjectGenerator generator;

		protected RenderObject(RenderObjectGenerator generator) {
			this.generator = generator;
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

		protected abstract void render(BlockPos pos, float partialTicks, int chunksFromPlayer);

	}

	public interface RenderObjectGenerator {

		boolean isEnabled();
		RenderObject getRenderObject(IBlockState state, BlockPos pos, WorldClient world);
		default void onBlockUpdate(Map<BlockPos, RenderObject> map, BlockPos pos, IBlockState state) {}

	}

}
