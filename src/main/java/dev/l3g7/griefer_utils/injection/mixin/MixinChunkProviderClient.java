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

package dev.l3g7.griefer_utils.injection.mixin;

import dev.l3g7.griefer_utils.features.world.KeepChunksLoaded;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.util.misc.ChunkCache;
import dev.l3g7.griefer_utils.util.misc.ServerCheck;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

@Mixin(ChunkProviderClient.class)
public abstract class MixinChunkProviderClient implements ChunkCache {

	private static final List<ChunkCoordIntPair> loadedChunks = new ArrayList<>();
	private static final List<ChunkCoordIntPair> unloadedChunks = new ArrayList<>();
	private static LongHashMap<Chunk> chunkCache = new LongHashMap<>();
	private static boolean doingManualOperation = false;

	@Shadow
	private LongHashMap<Chunk> chunkMapping;

	@Shadow
	private List<Chunk> chunkListing;

	private static boolean shouldTrigger() {
		return FileProvider.getSingleton(KeepChunksLoaded.class).isEnabled() && ServerCheck.isOnGrieferGames();
	}

	private static boolean shouldChunkBeLoaded(int x, int z, boolean capRenderDistance) {
		int renderDistance = mc().gameSettings.renderDistanceChunks;
		if (renderDistance < 5 || capRenderDistance)
			renderDistance = 5;

		return Math.abs(x - player().chunkCoordX) <= renderDistance
			&& Math.abs(z - player().chunkCoordZ) <= renderDistance;
	}

	private static long toHash(ChunkCoordIntPair c) {
		return ChunkCoordIntPair.chunkXZ2Int(c.chunkXPos, c.chunkZPos);
	}

	@Shadow
	public abstract Chunk provideChunk(int x, int z);

	@Inject(method = "provideChunk(II)Lnet/minecraft/world/chunk/Chunk;", at = @At("HEAD"), cancellable = true)
	public void injectProvideChunk(int x, int z, CallbackInfoReturnable<Chunk> cir) {
		if (!shouldTrigger())
			return;

		Chunk chunk = chunkCache.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(x, z));
		if (chunk != null)
			cir.setReturnValue(chunk);
	}

	@Inject(method = "loadChunk", at = @At("TAIL"))
	public void injectLoadChunkTail(int chunkX, int chunkZ, CallbackInfoReturnable<Chunk> cir) {
		if (doingManualOperation)
			return;

		Chunk chunk = provideChunk(chunkX, chunkZ);
		if (chunk != null)
			chunkCache.add(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ), chunk);

		if (shouldTrigger())
			handleChunks(true);
	}

	@Inject(method = "unloadChunk", at = @At("HEAD"), cancellable = true)
	public void injectUnloadChunkHead(int x, int z, CallbackInfo ci) {
		if (doingManualOperation || !shouldTrigger())
			return;

		if (shouldChunkBeLoaded(x, z, false))
			loadedChunks.add(new ChunkCoordIntPair(x, z));
		else
			unloadedChunks.add(new ChunkCoordIntPair(x, z));

		handleChunks(false);
		ci.cancel();
	}

	public void handleChunks(boolean load) {
		doingManualOperation = true;

		Iterator<ChunkCoordIntPair> iterator = (load ? unloadedChunks : loadedChunks).iterator();
		while (iterator.hasNext()) {
			ChunkCoordIntPair c = iterator.next();

			if (shouldChunkBeLoaded(c.chunkXPos, c.chunkZPos, false) != load)
				continue;

			Chunk chunk = chunkCache.getValueByKey(toHash(c));
			mc().getNetHandler().handleChunkData(new S21PacketChunkData(chunk, load, load ? 65535 : 0));

			iterator.remove();
			(load ? loadedChunks : unloadedChunks).add(c);
		}

		doingManualOperation = false;
	}

	public void clearCaches() {
		loadedChunks.clear();
		unloadedChunks.clear();
		chunkCache = new LongHashMap<>();
	}

	public void reset() {
		for (ChunkCoordIntPair c : loadedChunks) {
			if (!shouldChunkBeLoaded(c.chunkXPos, c.chunkZPos, true))
				continue;

			Chunk chunk = chunkCache.getValueByKey(toHash(c));
			chunkMapping.add(toHash(c), chunk);
			chunkListing.add(chunk);
		}

	}

}
