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

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.griefergames.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.SmallButtonSetting;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.main.ModTextures;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

@Singleton
public class KeepChunksLoaded extends Feature {

	private static final List<ChunkCoordIntPair> loadedChunks = new ArrayList<>();
	private static final List<ChunkCoordIntPair> unloadedChunks = new ArrayList<>();

	public static boolean keepChunkLoaded(int x, int z) {
		if (FileProvider.getSingleton(KeepChunksLoaded.class).isEnabled()) {
			if (shouldChunkBeLoaded(x, z, false))
				loadedChunks.add(new ChunkCoordIntPair(x, z));
			else
				unloadedChunks.add(new ChunkCoordIntPair(x, z));
			return true;
		}

		return false;
	}

	private static boolean shouldChunkBeLoaded(int x, int z, boolean capRenderDistance) {
		int renderDistance = mc().gameSettings.renderDistanceChunks;
		if (renderDistance < 5 || capRenderDistance)
			renderDistance = 5;

		return Math.abs(x - player().chunkCoordX) <= renderDistance
			&& Math.abs(z - player().chunkCoordZ) <= renderDistance;
	}

	private final SmallButtonSetting unloadButton = new SmallButtonSetting()
		.name("Unsichtbare Chunks entladen")
		.description("Entlädt alle Chunks außerhalb der derzeitigen Sichtweite.")
		.icon("arrow_circle")
		.buttonIcon(new ControlElement.IconData(ModTextures.BUTTON_TRASH))
		.callback(() -> unloadChunks(false));

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Chunks geladen lassen")
		.description("Lässt Chunks nicht entladen."
			+ "\nErmöglicht Sichtweiten von > 5 Chunks.")
		.icon("chunk")
		.subSettings(unloadButton)
		.callback(enabled -> {
			if (!enabled)
				unloadChunks(true);
		});

	@EventListener
	public void onCBSwitch(CityBuildJoinEvent event) {
		loadedChunks.clear();
		unloadedChunks.clear();
	}

	@EventListener
	public void onServerQuit(ServerEvent.ServerQuitEvent event) {
		loadedChunks.clear();
		unloadedChunks.clear();
	}

	@EventListener
	private void updateChunks(TickEvent.ClientTickEvent event) {
		if (world() == null)
			return;

		ChunkProviderClient provider = Reflection.get(world(), "clientChunkProvider");
		LongHashMap<Chunk> chunkMapping = Reflection.get(provider, "chunkMapping");

		List<ChunkCoordIntPair> unloadedChunksCopy = new ArrayList<>(unloadedChunks);
		unloadChunks(false);

		for (ChunkCoordIntPair c : unloadedChunksCopy) {
			if (!shouldChunkBeLoaded(c.chunkXPos, c.chunkZPos, false))
				continue;

			world().markBlockRangeForRenderUpdate(c.getXStart(), 0, c.getZStart(), c.getXEnd(), 256, c.getZEnd());
			if (!(world().provider instanceof WorldProviderSurface)) {
				Chunk chunk = chunkMapping.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(c.chunkXPos, c.chunkZPos));
				if (chunk != null)
					chunk.resetRelightChecks();
			}

			unloadedChunks.remove(c);
			loadedChunks.add(c);
		}
	}

	private void unloadChunks(boolean capRenderDistance) {
		if (world() == null)
			return;

		ChunkProviderClient provider = Reflection.get(world(), "clientChunkProvider");
		LongHashMap<Chunk> chunkMapping = Reflection.get(provider, "chunkMapping");

		// Unload chunks
		Iterator<ChunkCoordIntPair> iterator = loadedChunks.iterator();
		long startTime = System.currentTimeMillis();
		while (iterator.hasNext()) {
			ChunkCoordIntPair c = iterator.next();

			if (shouldChunkBeLoaded(c.chunkXPos, c.chunkZPos, capRenderDistance))
				continue;

			Chunk chunk = chunkMapping.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(c.chunkXPos, c.chunkZPos));
			if (chunk != null)
				chunk.func_150804_b(System.currentTimeMillis() - startTime > 5L);
			iterator.remove();
			unloadedChunks.add(c);
		}
	}

}
