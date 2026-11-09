/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.building.easy_place;

import dev.l3g7.griefer_utils.core.util.render.GeometryRenderUtil;
import dev.l3g7.griefer_utils.core.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;
import static net.minecraft.util.EnumFacing.values;
import static org.lwjgl.opengl.GL11.*;

class SideRenderer {

	private static final int[][] selectionIndices = new int[][]{
		new int[] {3, 1, 0, 2, 0, 0}, // X
		new int[] {0, 0, 0, 2, 3, 1}, // Y
		new int[] {0, 2, 0, 0, 3, 1}  // Z
	};

	private static Vec3 multiply(Vec3 a, Vec3i b) {
		return new Vec3(a.xCoord * b.getX(), a.yCoord * b.getY(), a.zCoord * b.getZ());
	}

	Vec3[] outer = new Vec3[4];
	Vec3[] inner = new Vec3[4];

	public SideRenderer(Vec3i[] masks) {
		Vec3 outerStart = outer[0] = multiply(new Vec3(-0.5f, -0.5f, -0.5f), masks[1]);
		Vec3 innerStart = inner[0] = multiply(new Vec3(-0.2f, -0.2f, -0.2f), masks[1]);

		for (int i = 0; i < masks.length; i++) {
			outer[i + 1] = outerStart.add(multiply(new Vec3(1f, 1f, 1f), masks[i]));
			inner[i + 1] = innerStart.add(multiply(new Vec3(0.4f, 0.4f, 0.4f), masks[i]));
		}
	}

	public void render(BlockPos pos, EnumFacing facing, EnumFacing selectedFacing) {
		GeometryRenderUtil.startLineDrawing(new Color(0, 0, 0, 196), 5f);
		GlStateManager.enableBlend();
		GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		Vec3 pos3d = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

		for (int i = 0; i < 4; i++) {
			line(outer[i].add(pos3d), inner[i].add(pos3d));
			line(outer[i].add(pos3d), outer[i == 3 ? 0 : i + 1].add(pos3d));
			line(inner[i].add(pos3d), inner[i == 3 ? 0 : i + 1].add(pos3d));
		}

		GeometryRenderUtil.finishLineDrawing();
		drawPolygon(pos3d, true, getSelectionIndex(facing, selectedFacing));

		for (EnumFacing value : values()) {
			if (value == facing)
				continue;

			BlockPos p = pos.offset(value);
			boolean isReplaceable = world().getBlockState(p).getBlock().isReplaceable(world(), p);
			if (!isReplaceable)
				drawPolygon(pos3d, false, getSelectionIndex(facing, value));
		}
	}

	private static void line(Vec3 start, Vec3 end) {
		GeometryRenderUtil.drawLineWhenRenderingStarted(
			(float) start.xCoord,
			(float) start.yCoord,
			(float) start.zCoord,
			(float) end.xCoord,
			(float) end.yCoord,
			(float) end.zCoord
		);
	}

	private void drawPolygon(Vec3 shift, boolean enabled, int selectedFace) {
		Color color = enabled
			? new Color(0, 196, 0, 128)
			: new Color(64, 64, 64, 196);

		if (selectedFace == 4) {
			polygon(color,
				inner[0].add(shift),
				inner[1].add(shift),
				inner[2].add(shift),
				inner[3].add(shift)
			);
			return;
		}

		polygon(color,
			outer[selectedFace].add(shift),
			outer[selectedFace == 3 ? 0 : selectedFace + 1].add(shift),
			inner[selectedFace == 3 ? 0 : selectedFace + 1].add(shift),
			inner[selectedFace].add(shift)
		);
	}

	private static void polygon(Color color, Vec3... vertices) {
		Vec3 cam = RenderUtil.camPos();
		GlStateManager.disableTexture2D();

		// Draw lines
		WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
		wr.begin(GL_POLYGON, DefaultVertexFormats.POSITION);
		GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

		for (int i = vertices.length - 1; i >= 0; i--) {
			Vec3 vertex = vertices[i];
			wr.pos(vertex.xCoord - cam.xCoord, vertex.yCoord - cam.yCoord, vertex.zCoord - cam.zCoord);
			wr.endVertex();
		}

		for (Vec3 vertex : vertices) {
			wr.pos(vertex.xCoord - cam.xCoord, vertex.yCoord - cam.yCoord, vertex.zCoord - cam.zCoord);
			wr.endVertex();
		}

		Tessellator.getInstance().draw();

		// Reset line width
		GlStateManager.enableTexture2D();
	}

	private static int getSelectionIndex(EnumFacing origin, EnumFacing target) {
		if (origin.getAxis() == target.getAxis())
			return 4;

		return selectionIndices[origin.getAxis().ordinal()][target.ordinal()];
	}

}
