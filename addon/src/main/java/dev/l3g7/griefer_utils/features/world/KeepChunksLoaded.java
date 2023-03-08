/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.core.util.Util;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.world;

@Singleton
public class KeepChunksLoaded extends Feature {

	private static final Set<ChunkCoordIntPair> forceLoadedChunks = new HashSet<>();

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Chunks geladen lassen")
		.icon("chunk")
		.description("Lässt Chunks nicht entladen.")
		.callback(b -> {
			if (b || world() == null)
				return;

			// Unload force loaded chunks
			for (ChunkCoordIntPair chunkCoords : forceLoadedChunks) {
				try {
					PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
					buf.writeInt(chunkCoords.chunkXPos);
					buf.writeInt(chunkCoords.chunkZPos);
					buf.writeBoolean(true);
					buf.writeShort(0);
					buf.writeByteArray(new byte[0]);
					S21PacketChunkData packet = new S21PacketChunkData();
					packet.readPacketData(buf);
					packet.processPacket(mc().getNetHandler());
				} catch (IOException e) {
					throw Util.elevate(e);
				}
			}
		});

	@EventListener
	public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
		if (event.packet instanceof S21PacketChunkData) {
			S21PacketChunkData packet = (S21PacketChunkData) event.packet;
			if (!packet.func_149274_i())
				return;

			ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(packet.getChunkX(), packet.getChunkZ());
			if (packet.getExtractedSize() == 0) {
				forceLoadedChunks.add(chunkCoords);
				event.setCanceled(true);
			} else {
				forceLoadedChunks.remove(chunkCoords);
			}
		}

		if (event.packet instanceof S26PacketMapChunkBulk) {
			S26PacketMapChunkBulk packet = (S26PacketMapChunkBulk) event.packet;
			for (int i = 0; i < packet.getChunkCount(); i++)
				forceLoadedChunks.remove(new ChunkCoordIntPair(packet.getChunkX(i), packet.getChunkZ(i)));
		}
	}

	public static ClassInheritanceMultiMap<Entity>[] getFilledEntityLists(Chunk chunk) {
		ClassInheritanceMultiMap<Entity>[] entityLists = chunk.getEntityLists();

		if (!FileProvider.getSingleton(KeepChunksLoaded.class).isEnabled())
			return entityLists;

		// Remove all entities
		for (ClassInheritanceMultiMap<Entity> map : entityLists) {
			List<Entity> entities = new ArrayList<>(Reflection.get(map, "values"));
			for (Entity entity : entities)
				map.remove(entity);
		}

		// Add them again
		for (Entity entity : world().loadedEntityList) {
			if (entity.chunkCoordX != chunk.xPosition || entity.chunkCoordZ != chunk.zPosition)
				continue;

			if (entity.chunkCoordY < 0 || entity.chunkCoordY > 15)
				continue;

			entityLists[entity.chunkCoordY].add(entity);
		}

		return entityLists;
	}

}
