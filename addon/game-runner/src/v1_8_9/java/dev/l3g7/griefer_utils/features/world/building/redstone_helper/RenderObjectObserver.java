/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.building.redstone_helper;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.ChunkFilledEvent;
import dev.l3g7.griefer_utils.core.events.ChunkUnloadEvent;
import dev.l3g7.griefer_utils.core.events.WorldUnloadEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.features.world.building.redstone_helper.Renderer.CompiledChunk;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class RenderObjectObserver {

	public static final Map<ChunkCoordIntPair, Chunk> data = new ConcurrentHashMap<>();

	@EventListener
	private static void onChunkFilled(ChunkFilledEvent event) {
		ChunkCoordIntPair coords = new ChunkCoordIntPair(event.getChunk().xPosition, event.getChunk().zPosition);
		Chunk chunk = new Chunk();

		for (ExtendedBlockStorage ebs : event.getChunk().getBlockStorageArray()) {
			if (ebs == null)
				continue;

			for (int x = 0; x < 16; x++) {
				for (int y = 0; y < 16; y++) {
					for (int z = 0; z < 16; z++) {
						BlockPos targetPos = new BlockPos(x, y + ebs.getYLocation(), z);
						RenderObject renderObject = RenderObject.fromState(targetPos, ebs.get(x, y, z)); // TODO: use data[] directly
						if (renderObject == null)
							continue;

						chunk.add(renderObject);
					}
				}
			}
		}

		if (chunk.hasData())
			delete(data.put(coords, chunk));
		else
			delete(data.remove(coords));
	}

	@EventListener
	private static void onChunkUnload(ChunkUnloadEvent event) {
		if (!event.chunk.getWorld().isRemote)
			return; // Integrated server chunk, its thread has no GL context

		ChunkCoordIntPair coordPair = new ChunkCoordIntPair(event.chunk.xPosition, event.chunk.zPosition);
		delete(data.remove(coordPair));
	}

	@EventListener
	private static void onWorldUnload(WorldUnloadEvent event) {
		for (Chunk chunk : data.values())
			chunk.delete();

		data.clear();
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

	private static void delete(Chunk chunk) {
		if (chunk != null)
			chunk.delete();
	}

	private static void updateBlock(BlockPos pos, IBlockState state) {
		ChunkCoordIntPair pair = new ChunkCoordIntPair(pos.getX() >> 4, pos.getZ() >> 4);
		BlockPos ebsPos = new BlockPos(pos.getX() - (pair.chunkXPos << 4), pos.getY(), pos.getZ() - (pair.chunkZPos << 4));
		RenderObject renderObject = RenderObject.fromState(ebsPos, state);

		if (renderObject != null) {
			Chunk chunk = data.computeIfAbsent(pair, k -> new Chunk());
			chunk.add(renderObject);
			return;
		}

		Chunk chunk = data.get(pair);
		if (chunk != null && chunk.hasData())
			chunk.remove(ebsPos);
	}

	static boolean isEnabled() {
		return RedstoneHelper.get().isEnabled();
	}

	public static class Chunk {

		public static void onSettingsChange() {
			for (Chunk chunk : data.values())
				for (ChunkPart part : chunk.parts.values())
					if (part.renderObjects.values().stream().anyMatch(r -> r.previousRenderState != r.shouldRender()))
						chunk.partsToRecompile.add(part);
		}

		private final Map<Integer, ChunkPart> parts = Collections.synchronizedMap(new HashMap<>());
		private final Set<ChunkPart> partsToRecompile = Collections.synchronizedSet(new HashSet<>());

		void add(RenderObject newObj) {
			ChunkPart part = parts.computeIfAbsent(newObj.pos.getY() / 16, k -> new ChunkPart());
			RenderObject prevObj = part.renderObjects.put(newObj.pos, newObj);
			if (newObj.equals(prevObj))
				return;

			if (prevObj == null) {
				if (newObj.shouldRender())
					partsToRecompile.add(part);
			} else {
				if (newObj.shouldRender() || prevObj.shouldRender())
					partsToRecompile.add(part);
			}
		}

		void remove(BlockPos pos) {
			ChunkPart part = parts.get(pos.getY() / 16);
			if (part == null)
				return;

			RenderObject prevObject = part.renderObjects.remove(pos);

			if (part.renderObjects.isEmpty()) {
				parts.remove(pos.getY() / 16);
				partsToRecompile.remove(part);
				part.delete();
			} else if (prevObject != null && prevObject.shouldRender())
				partsToRecompile.add(part);
		}

		void delete() {
			synchronized (parts) {
				for (ChunkPart part : parts.values())
					part.delete();

				parts.clear();
			}
			partsToRecompile.clear();
		}

		boolean hasData() {
			return !parts.isEmpty();
		}

		void draw(ChunkCoordIntPair pair, Frustum frustum, int rotation) {
			if (!partsToRecompile.isEmpty()) {
				synchronized (partsToRecompile) {
					partsToRecompile.removeIf(p -> {
						p.compile();
						return true;
					});
				}
			}

			synchronized (parts) {
				for (Map.Entry<Integer, ChunkPart> entry : parts.entrySet()) {
					boolean isVisible = frustum.isBoundingBoxInFrustum(new AxisAlignedBB(
						pair.getXStart(),
						entry.getKey() << 4,
						pair.getZStart(),
						pair.getXEnd(),
						entry.getKey() << 4 | 15,
						pair.getZEnd()));

					if (isVisible) {
						ChunkPart part = entry.getValue();
						if (!part.compiled)
							part.compile();

						if (part.list == 0)
							return;

						mc().getTextureManager().bindTexture(new ResourceLocation("textures/font/ascii.png"));
						GL11.glCallList(part.list + rotation);
					}
				}
			}
		}

		private static class ChunkPart {

			private static final CompiledChunk scratch = new CompiledChunk(80 * 256);

			int list = 0; // display list base per texture type, 0 if not allocated
			boolean compiled = false;
			Map<BlockPos, RenderObject> renderObjects = new ConcurrentHashMap<>();

			private void compile() {
				for (RenderObject ro : renderObjects.values())
					ro.previousRenderState = ro.shouldRender();

				if (renderObjects.values().stream().noneMatch(ro -> ro.previousRenderState)) {
					delete();
					compiled = true;
					return;
				}

				if (list == 0)
					list = GL11.glGenLists(16);

				if (list == 0)
					return; // glGenLists failed

				for (int rotation = 0; rotation < 16; rotation++) {
					scratch.reset();
					for (RenderObject ro : renderObjects.values())
						if (ro.previousRenderState)
							ro.draw(scratch, rotation);

					GL11.glNewList(list + rotation, GL11.GL_COMPILE);
					scratch.draw();
					GL11.glEndList();
				}

				compiled = true;
			}

			private void delete() {
				compiled = false;
				if (list == 0)
					return;

				GL11.glDeleteLists(list, 16);
				list = 0;
			}

		}

	}

}
