/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render.render_engine;

import com.github.lunatrius.schematica.api.ISchematic;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.util.SchematicaUtil;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;

import java.util.Objects;

@Singleton
class SchematicaRenderChunkManager extends RenderChunkManager {

	@Override
	public void reload() {
		previousSchematic = null;
		checkForSchematicUpdate();
	}

	@Override
	protected void checkRecompilation(boolean forceRecompilation) {
		if (!forceRecompilation)
			checkForSchematicUpdate();

		super.checkRecompilation(forceRecompilation);
	}

	private static Object previousSchematic = null;
	private static BlockPos previousSchematicPos = null;

	private void checkForSchematicUpdate() {
		ISchematic schematic = SchematicaUtil.getWorld() == null ? null : SchematicaUtil.getSchematic();
		BlockPos position = SchematicaUtil.getWorld() == null ? null : SchematicaUtil.getPosition();

		if (previousSchematic == schematic && Objects.equals(previousSchematicPos, position))
			return;

		previousSchematic = schematic;
		previousSchematicPos = position == null ? null : new BlockPos(position);

		RenderChunk.deleteAll(chunks.values());
		if (schematic == null || position == null)
			return;

		for (int dX = 0; dX < schematic.getWidth(); dX++) {
			for (int dY = 0; dY < schematic.getHeight(); dY++) {
				for (int dZ = 0; dZ < schematic.getLength(); dZ++) {
					BlockPos pos = new BlockPos(dX, dY, dZ);
					RenderObject renderObject = RenderObject.fromState(pos, schematic.getBlockState(pos), SchematicaUtil.getWorld());
					if (renderObject == null)
						continue;

					ChunkCoordIntPair pair = new ChunkCoordIntPair(pos.getX() >> 4, pos.getZ() >> 4);
					BlockPos worldPos = pos.add(dX, dY, dZ);
					BlockPos chunkPos = new BlockPos(worldPos.getX() & 15, worldPos.getY(), worldPos.getZ() & 15);
					chunks.computeIfAbsent(pair, p -> new RenderChunk())
						.add(chunkPos, renderObject);
				}
			}
		}
	}

}
