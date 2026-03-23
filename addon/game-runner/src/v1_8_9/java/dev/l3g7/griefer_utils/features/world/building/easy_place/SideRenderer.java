/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world.building.easy_place;

import dev.l3g7.griefer_utils.core.misc.Vec3d;
import dev.l3g7.griefer_utils.core.util.render.GlEngine;
import dev.l3g7.griefer_utils.core.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
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

	Vec3d[] outer = new Vec3d[4];
	Vec3d[] inner = new Vec3d[4];

	public SideRenderer(Vec3i[] masks) {
		Vec3d outerStart = outer[0] = new Vec3d(-0.5f, -0.5f, -0.5f).mul(masks[1]);
		Vec3d innerStart = inner[0] = new Vec3d(-0.2f, -0.2f, -0.2f).mul(masks[1]);

		for (int i = 0; i < masks.length; i++) {
			outer[i + 1] = outerStart.add(new Vec3d(1f, 1f, 1f).mul(masks[i]));
			inner[i + 1] = innerStart.add(new Vec3d(0.4f, 0.4f, 0.4f).mul(masks[i]));
		}
	}

	public void render(BlockPos pos, EnumFacing facing, EnumFacing selectedFacing) {
		RenderUtil.startLineDrawing(new Color(0, 0, 0, 196), 5f);
		GlStateManager.enableBlend();
		GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		Vec3d pos3d = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

		for (int i = 0; i < 4; i++) {
			line(outer[i].add(pos3d), inner[i].add(pos3d));
			line(outer[i].add(pos3d), outer[i == 3 ? 0 : i + 1].add(pos3d));
			line(inner[i].add(pos3d), inner[i == 3 ? 0 : i + 1].add(pos3d));
		}

		RenderUtil.finishLineDrawing();
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

	private static void line(Vec3d start, Vec3d end) {
		RenderUtil.drawLineWhenRenderingStarted((float) start.x, (float) start.y, (float) start.z, (float) end.x, (float) end.y, (float) end.z);
	}

	private void drawPolygon(Vec3d shift, boolean enabled, int selectedFace) {
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

	private static void polygon(Color color, Vec3d... vertices) {
		Vec3 cam = RenderUtil.camPos();
		GlStateManager.disableTexture2D();

		// Draw lines
		GlEngine.begin();
		WorldRenderer buf = GlEngine.beginWorldDrawing(GL_POLYGON, DefaultVertexFormats.POSITION);
		GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

		for (int i = vertices.length - 1; i >= 0; i--) {
			Vec3d vertex = vertices[i];
			buf.pos(vertex.x - cam.xCoord, vertex.y - cam.yCoord, vertex.z - cam.zCoord);
			buf.endVertex();
		}

		for (Vec3d vertex : vertices) {
			buf.pos(vertex.x - cam.xCoord, vertex.y - cam.yCoord, vertex.z - cam.zCoord);
			buf.endVertex();
		}

		GlEngine.finish();

		// Reset line width
		GlStateManager.enableTexture2D();
	}

	private static int getSelectionIndex(EnumFacing origin, EnumFacing target) {
		if (origin.getAxis() == target.getAxis())
			return 4;

		return selectionIndices[origin.getAxis().ordinal()][target.ordinal()];
	}

}
