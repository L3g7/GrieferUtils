/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.building.redstone_helper;

import dev.l3g7.griefer_utils.features.world.building.redstone_helper.Renderer.CompiledChunk;
import dev.l3g7.griefer_utils.features.world.building.redstone_helper.FontRenderData.CharRenderInfo;
import dev.l3g7.griefer_utils.features.world.building.redstone_helper.FontRenderData.TextureUV;
import dev.l3g7.griefer_utils.features.world.building.redstone_helper.FontRenderData.Vertices;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static net.minecraft.init.Blocks.hopper;
import static net.minecraft.init.Blocks.redstone_wire;

public abstract class RenderObject {

	public static RenderObject fromState(BlockPos pos, IBlockState state) {
		if (state == null)
			return null;

		Block block = state.getBlock();
		if (block == redstone_wire)
			return new RedstoneHelper.Wire(pos, state.getValue(BlockRedstoneWire.POWER));

		if (block == hopper) {
			EnumFacing facing = state.getValue(BlockHopper.FACING);
			return facing.getHorizontalIndex() == -1 ? null : new RedstoneHelper.Hopper(pos, facing);
		}

		return null;
	}

	public final TextureType textureType;
	public final BlockPos pos;
	public boolean previousRenderState = false;

	protected RenderObject(TextureType textureType, BlockPos pos) {
		this.textureType = textureType;
		this.pos = pos;
	}

	public abstract double getYOffset();
	public abstract CharRenderInfo[] getRenderInfos();
	public abstract Vertices getVertices(CharRenderInfo charRenderInfo, int rotation);

	public abstract boolean shouldRender();

	public boolean equals(RenderObject r) {
		return getClass().isInstance(r);
	}

	public void draw(CompiledChunk[] chunks) {
		if (!(previousRenderState = shouldRender()))
			return;

		for (int i = 0; i < chunks.length; i++)
			draw(chunks[i], i);
	}

	public void draw(CompiledChunk chunk, int rotation) {
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

	public enum TextureType {
		ROTATING_TEXT(true, new ResourceLocation("textures/font/ascii.png")),
		ARROWS(false, new ResourceLocation("textures/font/unicode_page_2b.png"));

		private final boolean rotates;
		private final ResourceLocation texture;

		TextureType(boolean rotates, ResourceLocation texture) {
			this.rotates = rotates;
			this.texture = texture;
		}

		public int chunks() {
			return rotates ? 16 : 1;
		}

		public void draw(CompiledChunk[][] compiledChunks, int rotation) {
			mc().getTextureManager().bindTexture(texture);
			CompiledChunk[] chunks = compiledChunks[ordinal()];
			// TODO why can chunks include nulls?
			if (chunks != null && chunks[rotates ? rotation : 0] != null)
				chunks[rotates ? rotation : 0].draw();
		}
	}

}
