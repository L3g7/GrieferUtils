/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc;

import net.labymod.api.client.gfx.pipeline.buffer.BufferBuilder;
import net.labymod.api.client.gfx.pipeline.texture.atlas.TextureAtlas;
import net.labymod.api.client.gfx.pipeline.texture.atlas.TextureUV;
import net.labymod.core.client.gfx.pipeline.texture.atlas.DefaultTextureUV;
import net.labymod.core.client.render.schematic.SchematicAccessor;
import net.labymod.core.client.render.schematic.block.Block;
import net.labymod.core.client.render.schematic.block.BlockRenderer;
import net.labymod.core.client.render.schematic.block.Face;
import net.labymod.core.client.render.schematic.block.material.BoundingBox;
import net.labymod.core.client.render.schematic.block.material.material.SolidMaterial;

import static net.labymod.core.client.render.schematic.block.Face.*;

public class SkullMaterial extends SolidMaterial {

	private static final Face[] FACES = new Face[] {EAST, NORTH, TOP, EAST, NORTH, TOP};
	private static final TextureUV[] UVs = new TextureUV[] {
		createUV(48, 184), // G
		createUV(56, 184), // U
		createUV(56, 176), //
		createUV(64, 184), // G 2
		createUV(72, 184), // U 2
		createUV(72, 176)  //   2
	};

	public SkullMaterial() {
		super("player_head");
	}

	private static TextureUV createUV(int x, int y) {
		float minU = x / 256f;
		float minV = y / 256f;
		float maxU = (x + 8) / 256f;
		float maxV = (y + 8) / 256f;
		return new DefaultTextureUV(minU, minV, maxU, maxV);
	}

	private BoundingBox getBoundingBox(boolean secondLayer) {
		float add = secondLayer ? 1 / 32f : 0;

		BoundingBox bb = new BoundingBox(0.25f - add, 0.0f - add, 0.25f - add, 0.75f + add, 0.5f + add, 0.75f + add);
		bb.rotateY(0.5f, 0.25f, 0.5f, -10f);
		return bb;
	}

	@Override
	public BoundingBox getBoundingBox(SchematicAccessor level, int x, int y, int z, Block block) {
		return getBoundingBox(false);
	}

	@Override
	public void render(BlockRenderer renderer, TextureAtlas atlas, BufferBuilder builder, Block block, SchematicAccessor level, int x, int y, int z, BoundingBox bb) {
		BoundingBox secondLayer = getBoundingBox(true);

		for (int i = 0; i < FACES.length; i++)
			renderer.renderFace(builder, block, FACES[i], x, y, z, i > 2 ? secondLayer : bb, UVs[i]);
	}

	@Override
	public boolean isFullBlock() {
		return false;
	}

}
