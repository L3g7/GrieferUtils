/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render.render_engine;

import com.github.lunatrius.schematica.api.ISchematic;
import dev.l3g7.griefer_utils.core.util.SchematicaUtil;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates {@link RenderChunk}s and updates their data
 */
class SchematicaRenderChunkManager {

	static final Map<ChunkCoordIntPair, RenderChunk> schematicChunks = new ConcurrentHashMap<>();

	private static Object previousSchematic = null;
	private static BlockPos previousSchematicPos = null;

	public static void checkForSchematicUpdate() {
		ISchematic schematic = SchematicaUtil.getWorld() == null ? null : SchematicaUtil.getSchematic();
		BlockPos position = SchematicaUtil.getWorld() == null ? null : SchematicaUtil.getPosition();

		if (previousSchematic == schematic && Objects.equals(previousSchematicPos, position))
			return;

		previousSchematic = schematic;
		previousSchematicPos = position == null ? null : position.up().down();

		if (schematic == null || position == null) {
			RenderChunk.deleteAll(schematicChunks.values());
			return;
		}

		for (int dX = 0; dX < schematic.getWidth(); dX++) {
			for (int dY = 0; dY < schematic.getHeight(); dY++) {
				for (int dZ = 0; dZ < schematic.getLength(); dZ++) {
					BlockPos pos = new BlockPos(dX, dY, dZ);
					RenderObject renderObject = RenderObject.fromState(pos, schematic.getBlockState(pos), SchematicaUtil.getWorld());
					if (renderObject == null)
						continue;

					ChunkCoordIntPair pair = new ChunkCoordIntPair(pos.getX() >> 4, pos.getZ() >> 4);
					schematicChunks.computeIfAbsent(pair, p -> new RenderChunk())
						.add(position.add(dX, dY, dZ), renderObject);
				}
			}
		}
	}

}
