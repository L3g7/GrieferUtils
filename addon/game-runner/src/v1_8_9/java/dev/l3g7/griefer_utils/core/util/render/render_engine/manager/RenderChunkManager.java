/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render.render_engine.manager;

import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.util.render.render_engine.RenderChunk;
import dev.l3g7.griefer_utils.core.util.render.render_engine.RenderObject;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates {@link RenderChunk}s and keeps their data up to date.
 */
public abstract class RenderChunkManager {

	public static final List<RenderChunkManager> managers = new ArrayList<>() {{
		add(WorldRenderChunkManager.get());

		if (Constants.SCHEMATICA) {
			//noinspection StaticInitializerReferencesSubClass (it works lol)
			add(FileProvider.getSingleton(SchematicaRenderChunkManager.class));
		}
	}};

	/**
	 * Forces all manager to rescan everything, e.g. after a setting changed.
	 */
	public static void reloadAll() {
		for (RenderChunkManager manager : managers)
			manager.rescan();
	}

	/**
	 * All chunks containing at least one {@link RenderObject}.
	 */
	public final Map<ChunkCoordIntPair, RenderChunk> chunks = new ConcurrentHashMap<>();

	/**
	 * Forces this manager to rescan everything, e.g. after a setting changed.
	 */
	protected abstract void rescan();

	/**
	 * Sets the render object at the given position in the world.
	 * If there already was a render object, it is overwritten.
	 * If this method removes the last render object in a chunk, the whole chunk is removed.
	 * @param worldPos The render object's position in the world.
	 * @param renderObject The new render object, or null if there is none.
	 */
	public void setRenderObject(BlockPos worldPos, RenderObject renderObject) {
		ChunkCoordIntPair pair = new ChunkCoordIntPair(worldPos.getX() >> 4, worldPos.getZ() >> 4);
		BlockPos chunkPos = new BlockPos(worldPos.getX() & 15, worldPos.getY(), worldPos.getZ() & 15);

		if (renderObject != null) {
			chunks.computeIfAbsent(pair, p -> new RenderChunk())
				.add(chunkPos, renderObject);
			return;
		}

		RenderChunk chunk = chunks.get(pair);
		if (chunk != null && chunk.hasData())
			chunk.remove(chunkPos);
	}

	/**
	 * Checks whether any chunks need recompilation.
	 * @param forceRecompilation Whether all chunks should forcefully be recompiled.
	 */
	public void checkRecompilation(boolean forceRecompilation) {
		for (RenderChunk chunk : chunks.values())
			chunk.checkRecompile(forceRecompilation);
	}

}
