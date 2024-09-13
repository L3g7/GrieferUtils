/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.redstone_helper;

import dev.l3g7.griefer_utils.features.world.RedstoneHelper;
import dev.l3g7.griefer_utils.features.world.redstone_helper.Renderer.CompiledChunk;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static net.minecraft.init.Blocks.*;

public abstract class RenderObject {

	public static RenderObject fromState(BlockPos pos, IBlockState state) {
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
	public abstract double[] getTexData();
	public abstract double[] getOffsets(int rotation);

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

	@SuppressWarnings("PointlessArithmeticExpression")
	public void draw(CompiledChunk chunk, int rotation) {
		double x = (double) pos.getX() + 0.5d;
		double y = (double) pos.getY() + getYOffset();
		double z = (double) pos.getZ() + 0.5d;

		double[] texData = getTexData();
		double[] offsets = getOffsets(rotation);

		for (int i = 0; i < texData.length / 4; i++) {
			int oo = i * 8;
			int to = i * 4;

			chunk.ensureCapacity();
			chunk.pos(x + offsets[0 + oo], y, z + offsets[4 + oo]).tex(texData[1 + to], texData[3 + to]).endVertex();
			chunk.pos(x + offsets[1 + oo], y, z + offsets[5 + oo]).tex(texData[0 + to], texData[3 + to]).endVertex();
			chunk.pos(x + offsets[2 + oo], y, z + offsets[6 + oo]).tex(texData[0 + to], texData[2 + to]).endVertex();
			chunk.pos(x + offsets[3 + oo], y, z + offsets[7 + oo]).tex(texData[1 + to], texData[2 + to]).endVertex();
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
