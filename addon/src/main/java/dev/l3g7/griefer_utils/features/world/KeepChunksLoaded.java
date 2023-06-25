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

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.world.ChunkCoordIntPair;

import java.util.HashSet;
import java.util.Set;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.world;

@Singleton
public class KeepChunksLoaded extends Feature {

	private final Set<ChunkCoordIntPair> forceLoadedChunks = new HashSet<>();

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Chunks geladen lassen")
		.icon("chunk")
		.description("Lässt Chunks nicht entladen.")
		.callback(enabled -> {
			if (enabled)
				return;

			// Unload chunks
			if (world() == null)
				return;

			for (ChunkCoordIntPair chunkCoords : forceLoadedChunks)
				world().doPreChunk(chunkCoords.chunkXPos, chunkCoords.chunkZPos, false);
		});

	@EventListener
	public void onPacketReceive(PacketReceiveEvent event) {
		if (event.packet instanceof S21PacketChunkData) {
			S21PacketChunkData packet = (S21PacketChunkData) event.packet;
			// Only check if update contains block updates
			if (!packet.func_149274_i())
				return;

			ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(packet.getChunkX(), packet.getChunkZ());
			if (packet.getExtractedSize() == 0)
				// Chunk is empty -> should be unloaded, force loading
				forceLoadedChunks.add(chunkCoords);
			else
				// Chunk should be loaded, forcing isn't needed
				forceLoadedChunks.remove(chunkCoords);
		}

		else if (event.packet instanceof S26PacketMapChunkBulk) {
			// Chunk should be loaded, forcing isn't needed
			S26PacketMapChunkBulk packet = (S26PacketMapChunkBulk) event.packet;
			for (int i = 0; i < packet.getChunkCount(); i++)
				forceLoadedChunks.remove(new ChunkCoordIntPair(packet.getChunkX(i), packet.getChunkZ(i)));
		}
	}

}
