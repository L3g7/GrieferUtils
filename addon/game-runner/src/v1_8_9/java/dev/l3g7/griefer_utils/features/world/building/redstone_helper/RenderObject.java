/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.building.redstone_helper;

import dev.l3g7.griefer_utils.features.world.building.redstone_helper.FontRenderData.CharRenderInfo;
import dev.l3g7.griefer_utils.features.world.building.redstone_helper.FontRenderData.TextureUV;
import dev.l3g7.griefer_utils.features.world.building.redstone_helper.FontRenderData.Vertices;
import dev.l3g7.griefer_utils.features.world.building.redstone_helper.render_pipeline.ChunkCompiler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;

import static net.minecraft.init.Blocks.redstone_wire;

public abstract class RenderObject {

	public static RenderObject fromState(BlockPos pos, IBlockState state) {
		if (state == null)
			return null;

		Block block = state.getBlock();
		if (block == redstone_wire)
			return new RedstoneHelper.Wire(pos, state.getValue(BlockRedstoneWire.POWER));

		return null;
	}

	public final BlockPos pos;

	protected RenderObject(BlockPos pos) {
		this.pos = pos;
	}

	public abstract double getYOffset();
	public abstract CharRenderInfo[] getRenderInfos();
	public abstract Vertices getVertices(CharRenderInfo charRenderInfo, int rotation);

	public boolean equals(RenderObject r) {
		return getClass().isInstance(r);
	}

	public void draw(ChunkCompiler chunk, int rotation) {
		double x = (double) pos.getX() + 0.5d;
		double y = (double) pos.getY() + getYOffset();
		double z = (double) pos.getZ() + 0.5d;

		for (CharRenderInfo charRenderInfo : getRenderInfos()) {
			Vertices offsets = getVertices(charRenderInfo, rotation);
			TextureUV texData = charRenderInfo.textureUV();

			chunk.ensureCapacity();
			chunk.pos(x + offsets.topLeft()    .x(), y, z + offsets.topLeft()    .y()).tex(texData.maxX(), texData.maxY()).endVertex();
			chunk.pos(x + offsets.topRight()   .x(), y, z + offsets.topRight()   .y()).tex(texData.minX(), texData.maxY()).endVertex();
			chunk.pos(x + offsets.bottomRight().x(), y, z + offsets.bottomRight().y()).tex(texData.minX(), texData.minY()).endVertex();
			chunk.pos(x + offsets.bottomLeft() .x(), y, z + offsets.bottomLeft() .y()).tex(texData.maxX(), texData.minY()).endVertex();
		}
	}

}
