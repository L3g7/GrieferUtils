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

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.List;
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
	public void onPacketReceive(PacketReceiveEvent<Packet<?>> event) {
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


	@Mixin(ChunkProviderClient.class)
	private static abstract class MixinChunkProviderClient {

		@Shadow
		public abstract Chunk provideChunk(int x, int z);

		@Shadow
		private LongHashMap<Chunk> chunkMapping;

		@Shadow
		private List<Chunk> chunkListing;

		@Inject(method = "unloadChunk", at = @At("HEAD"), cancellable = true)
		public void injectUnloadChunkHead(int x, int z, CallbackInfo ci) {
			if (FileProvider.getSingleton(KeepChunksLoaded.class).isEnabled())
				ci.cancel();
		}

		/**
		 * OptiFine keeps a list of which chunks contain entities for rending optimization
		 * (RenderGlobal#renderInfosEntities). When updating this list, a reference to the chunk in
		 * {@link RenderChunk} is used to check whether the chunk contains entities. Due to suppressing the
		 * unloading of a chunk stored in such a RenderChunk, the chunk remains loaded and the reference
		 * seems up-to-date. As a result, it isn't updated to new chunks and the old chunk gets checked. To
		 * fix invalid rendering of entities, old chunks get unloaded if a new one is loaded in the same
		 * position, making the reference outdated.
		 *
		 * @see RenderGlobal
		 * @see RenderChunk
		 */
		@Inject(method = "loadChunk", at = @At("HEAD"))
		public void injectLoadChunkHead(int x, int z, CallbackInfoReturnable<Chunk> cir) {
			if (!FileProvider.getSingleton(KeepChunksLoaded.class).isEnabled())
				return;

			Chunk chunk = provideChunk(x, z);

			if (!chunk.isEmpty())
				chunk.onChunkUnload();

			chunkMapping.remove(ChunkCoordIntPair.chunkXZ2Int(x, z));
			chunkListing.remove(chunk);
		}

	}

}
