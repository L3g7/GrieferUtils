/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render.render_engine.manager;

import com.github.lunatrius.schematica.api.ISchematic;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.util.SchematicaUtil;
import dev.l3g7.griefer_utils.core.util.render.render_engine.RenderChunk;
import dev.l3g7.griefer_utils.core.util.render.render_engine.RenderObject;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;

import java.util.Objects;

/**
 * The {@link RenderChunkManager} responsible for {@link RenderObject RenderObjects} in schematics.
 */
@Singleton
class SchematicaRenderChunkManager extends RenderChunkManager {

	private ISchematic schematic = null;
	private BlockPos schematicPos = null;

	@Override
	public void rescan() {
		schematic = null;
		if (checkSchematicUpdate())
			rescanSchematic();
	}

	@Override
	public void checkRecompilation(boolean forceRecompilation) {
		if (!forceRecompilation && checkSchematicUpdate())
			rescanSchematic();

		super.checkRecompilation(forceRecompilation);
	}

	/**
	 * @return Whether the schematic has changed since the last check.
	 */
	private boolean checkSchematicUpdate() {
		ISchematic newSchematic = SchematicaUtil.getWorld() == null ? null : SchematicaUtil.getSchematic();
		BlockPos position = SchematicaUtil.getWorld() == null ? null : SchematicaUtil.getPosition();

		if (schematic == newSchematic && Objects.equals(schematicPos, position))
			return false;

		schematic = newSchematic;
		schematicPos = position == null ? null : new BlockPos(position);
		return true;
	}

	/**
	 * Rescans the entire schematic for {@link RenderObject RenderObjects}.
	 */
	private void rescanSchematic() {
		RenderChunk.deleteAll(chunks.values());
		if (schematic == null || schematicPos == null)
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
