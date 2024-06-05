/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.better_hopper;

import dev.l3g7.griefer_utils.core.misc.Vec3d;
import dev.l3g7.griefer_utils.core.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
import java.util.Set;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static net.minecraft.util.EnumFacing.*;
import static org.lwjgl.opengl.GL11.*;

public class BlockyRenderSphere {

	static BlockyRenderSphere ORIGIN = new OriginBuilder().build();
	private static final int RADIUS_SQ = 30 * 30;

	public static BlockyRenderSphere getSphere(BlockPos origin) {
		SpherePart[] translatedParts = new SpherePart[ORIGIN.parts.length];

		for (int i = 0; i < ORIGIN.parts.length; i++)
			translatedParts[i] = ORIGIN.parts[i].translate(origin);

		return new BlockyRenderSphere(translatedParts);
	}

	private final SpherePart[] parts;

	private BlockyRenderSphere(SpherePart[] parts) {
		this.parts = parts;
	}

	public void render() {
		GlStateManager.pushMatrix();
		Entity entity = mc().getRenderViewEntity();
		Vec3d prevPos = new Vec3d(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
		Vec3d cam = prevPos.add(pos(entity).subtract(prevPos).scale(partialTicks()));

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION);
//		drawUtils().bindTexture("textures/blocks/wool_colored_red.png");

		GL11.glColor4f(1, 0, 0, 0.3f);
		GlStateManager.enableBlend();
		GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableTexture2D();

		for (SpherePart part : parts)
			part.render(cam);

		tessellator.draw();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
		GlStateManager.popMatrix();
	}

	public static class SpherePart {

		private final BlockPos pos;
		private final AxisAlignedBB bb;
		private final boolean down;
		private final boolean up;
		private final boolean north;
		private final boolean south;
		private final boolean west;
		private final boolean east;

		private SpherePart(BlockPos pos, boolean down, boolean up, boolean north, boolean south, boolean west, boolean east) {
			this.pos = pos;
			this.bb = new AxisAlignedBB(pos, pos.add(1, 1, 1)).expand(-0.0001, -0.0001, -0.0001);
			this.down = down;
			this.up = up;
			this.north = north;
			this.south = south;
			this.west = west;
			this.east = east;
		}

		private boolean isFullyBlocked() {
			return !(down || up || north || south || west || east);
		}

		private SpherePart mirror(boolean x, boolean y, boolean z) {
			BlockPos flippedPos = new BlockPos(
				pos.getX() * (x ? -1 : 1),
				pos.getY() * (y ? -1 : 1),
				pos.getZ() * (z ? -1 : 1)
			);

			return new SpherePart(flippedPos,
				y ? this.up : this.down,
				y ? this.down : this.up,
				z ? this.south : this.north,
				z ? this.north : this.south,
				x ? this.east : this.west,
				x ? this.west : this.east
			);
		}

		private SpherePart translate(BlockPos origin) {
			return new SpherePart(pos.add(origin),
				this.down,
				this.up,
				this.north,
				this.south,
				this.west,
				this.east
			);
		}

		private void render(Vec3d cam) {
			AxisAlignedBB bb = this.bb.offset(-cam.x, -cam.y, -cam.z);

			if (up)
				RenderUtil.drawFace(bb, UP, true);
			if (down)
				RenderUtil.drawFace(bb, DOWN, true);
			if (north)
				RenderUtil.drawFace(bb, NORTH, true);
			if (south)
				RenderUtil.drawFace(bb, SOUTH, true);
			if (west)
				RenderUtil.drawFace(bb, WEST, true);
			if (east)
				RenderUtil.drawFace(bb, EAST, true);
		}

		@Override
		public int hashCode() {
			return (pos.getY() + pos.getZ() * 97) * 97 + pos.getX();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;

			if (!(obj instanceof SpherePart))
				return false;

			SpherePart sp = (SpherePart) obj;
			return hashCode() == sp.hashCode();
		}
	}

	/**
	 * Could be optimized, but since it's only called once it doesn't really matter
	 */
	static class OriginBuilder {
		private final boolean[][][] validBlocks = new boolean[31][31][31];

		OriginBuilder() {
			for (int x = 0; x <= 30; x++)
				for (int y = 0; y <= 30; y++)
					for (int z = 0; z <= 30; z++)
						validBlocks[x][y][z] = x*x + y*y + z*z <= RADIUS_SQ;
		}

		BlockyRenderSphere build() {
			Set<SpherePart> parts = new HashSet<>();

			for (int x = 30; x >= 0; x--) {
				for (int y = 30; y >= 0; y--) {
					for (int z = 30; z >= 0; z--) {
						if (!validBlocks[x][y][z])
							continue;

						BlockPos pos = new BlockPos(x, y, z);
						SpherePart part = new SpherePart(pos,
							isNotValid(pos, EnumFacing.DOWN),
							isNotValid(pos, EnumFacing.UP),
							isNotValid(pos, EnumFacing.NORTH),
							isNotValid(pos, EnumFacing.SOUTH),
							isNotValid(pos, EnumFacing.WEST),
							isNotValid(pos, EnumFacing.EAST)
						);

						if (part.isFullyBlocked())
							continue;

						parts.add(part);
						parts.add(part.mirror(true, false, false));
						parts.add(part.mirror(false, false, true));
						parts.add(part.mirror(true, false, true));
						parts.add(part.mirror(false, true, false));
						parts.add(part.mirror(true, true, false));
						parts.add(part.mirror(false, true, true));
						parts.add(part.mirror(true, true, true));
					}
				}
			}

			return new BlockyRenderSphere(parts.toArray(new SpherePart[0]));
		}

		private boolean isNotValid(BlockPos pos, EnumFacing facing) {
			int x = pos.getX() + facing.getFrontOffsetX();
			int y = pos.getY() + facing.getFrontOffsetY();
			int z = pos.getZ() + facing.getFrontOffsetZ();

			if (x < 0)
				x = 1;
			if (y < 0)
				y = 1;
			if (z < 0)
				z = 1;

			if (x > 30 || y > 30 || z > 30)
				return true;

			return !validBlocks[x][y][z];
		}

	}

}
