/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.world.redstone_helper;

import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.events.ChunkFilledEvent;
import dev.l3g7.griefer_utils.core.events.ChunkUnloadEvent;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent;
import dev.l3g7.griefer_utils.features.world.RedstoneHelper;
import dev.l3g7.griefer_utils.features.world.redstone_helper.RenderObject.TextureType;
import dev.l3g7.griefer_utils.features.world.redstone_helper.Renderer.CompiledChunk;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
						BlockPos targetPos = coords.getBlock(x, y + ebs.getYLocation(), z);
						RenderObject renderObject = RenderObject.fromState(targetPos, ebs.get(x, y, z)); // TODO: use data[] directly
						if (renderObject == null)
							continue;

						chunk.add(renderObject);
					}
				}
			}
		}

		if (chunk.hasData())
			data.put(coords, chunk);
	}

	@EventListener
	private static void onChunkUnload(ChunkUnloadEvent event) {
		ChunkCoordIntPair coordPair = new ChunkCoordIntPair(event.chunk.xPosition, event.chunk.zPosition);
		data.remove(coordPair);
	}

	@EventListener
	private static void onServerChange(ServerEvent.ServerSwitchEvent event) {
		data.clear();
	}

	@EventListener
	private static void onPacket(PacketEvent.PacketReceiveEvent<Packet<?>> event) {
		if (event.packet instanceof S23PacketBlockChange) {
			S23PacketBlockChange packet = (S23PacketBlockChange) event.packet;
			updateBlock(packet.getBlockPosition(), packet.getBlockState());
		}

		if (!(event.packet instanceof S22PacketMultiBlockChange))
			return;

		S22PacketMultiBlockChange packet = (S22PacketMultiBlockChange) event.packet;
		for (S22PacketMultiBlockChange.BlockUpdateData data : packet.getChangedBlocks())
			updateBlock(data.getPos(), data.getBlockState());
	}

	private static void updateBlock(BlockPos pos, IBlockState state) {
		RenderObject renderObject = RenderObject.fromState(pos, state);

		ChunkCoordIntPair pair = new ChunkCoordIntPair(pos.getX() >> 4, pos.getZ() >> 4);

		if (renderObject != null) {
			Chunk chunk = data.computeIfAbsent(pair, k -> new Chunk());
			chunk.add(renderObject);
			return;
		}

		Chunk chunk = data.get(pair);
		if (chunk != null && chunk.hasData())
			chunk.remove(pos);
	}

	static boolean isEnabled() {
		return FileProvider.getSingleton(RedstoneHelper.class).isEnabled();
	}

	public static class Chunk {

		public static void onSettingsChange() {
			for (Chunk chunk : data.values())
				for (ChunkPart part : chunk.parts.values())
					if (part.renderObjects.values().stream().anyMatch(r -> r.previousRenderState != r.shouldRender()))
						chunk.partsToRecompile.add(part);
		}

		private final Map<Integer, ChunkPart> parts = new HashMap<>();
		private final Set<ChunkPart> partsToRecompile = new HashSet<>();

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

			if (part.renderObjects.isEmpty())
				parts.remove(pos.getY() / 16);
			else if (prevObject != null && prevObject.shouldRender())
				partsToRecompile.add(part);
		}

		boolean hasData() {
			return !parts.isEmpty();
		}

		void draw(ChunkCoordIntPair pair, Frustum frustum, int rotation) {
			if (!partsToRecompile.isEmpty()) {
				List<ChunkPart> parts = new ArrayList<>(partsToRecompile);
				for (ChunkPart chunkPart : parts)
					chunkPart.recompile();

				parts.forEach(partsToRecompile::remove);
			}

			for (Map.Entry<Integer, ChunkPart> entry : parts.entrySet()) {
				boolean isVisible = frustum.isBoundingBoxInFrustum(new AxisAlignedBB(
					pair.getXStart(),
					entry.getKey() << 4,
					pair.getZStart(),
					pair.getXEnd(),
					entry.getKey() << 4 | 15,
					pair.getZEnd()));

				if (isVisible) {
					if (entry.getValue().compiledChunks == null)
						entry.getValue().recompile();

					for (TextureType value : TextureType.values())
						value.draw(entry.getValue().compiledChunks, rotation);
				}
			}
		}

		private static class ChunkPart {

			CompiledChunk[][] compiledChunks = null;
			Map<BlockPos, RenderObject> renderObjects = new ConcurrentHashMap<>();

			private void recompile() {
				int[] size = new int[TextureType.values().length];
				for (RenderObject ro : renderObjects.values())
					size[ro.textureType.ordinal()] += ro.getTexData().length / 4;

				compiledChunks = new CompiledChunk[size.length][];
				for (int i = 0; i < TextureType.values().length; i++) {
					if (size[i] == 0)
						continue;

					TextureType type = TextureType.values()[i];
					compiledChunks[i] = new CompiledChunk[type.chunks()];

					for (int j = 0; j < compiledChunks[i].length; j++)
						compiledChunks[i][j] = new CompiledChunk(size[i] * 80);

					for (RenderObject renderObject : renderObjects.values())
						if (renderObject.textureType == type)
							renderObject.draw(compiledChunks[i]);
				}
			}

		}

	}

}
