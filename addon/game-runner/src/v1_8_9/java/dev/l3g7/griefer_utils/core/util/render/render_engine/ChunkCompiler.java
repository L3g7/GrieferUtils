/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render.render_engine;

import dev.l3g7.griefer_utils.core.util.render.render_engine.data.Quad;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.TextureCutout;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.UV;
import dev.l3g7.griefer_utils.core.util.render.sinks.VertexSink;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class ChunkCompiler implements VertexSink {

	/**
	 * The position of the RenderObject currently being drawing
	 */
	private BlockPos blockPos;
	private Color currentColor = Color.WHITE;
	private boolean isActive = false;

	private final Map<Integer, Boolean> capabilities = new HashMap<>();

	public void ensureActiveState(boolean active) {
		if (isActive == active)
			return;

		if (active)
			glBegin(GL_QUADS);
		else
			glEnd();

		isActive = active;
	}

	/**
	 * Prepares the compiler for drawing a new {@link RenderObject}
	 * @param blockPos The RenderObject's positions
	 */
	public void prepare(BlockPos blockPos) {
		setColor(Color.WHITE);
		this.blockPos = blockPos;
	}

	/**
	 * Begins drawing into a GL list.
	 * @param list The list's id.
	 */
	public void begin(int list) {
		glNewList(list, GL_COMPILE);
	}

	/**
	 * Ends drawing into the current GL list.
	 */
	public void end() {
		setColor(Color.WHITE);
		capabilities.clear();
		ensureActiveState(false);
		glEndList();
	}

	/**
	 * Sets the current color.
	 * If it's not equal to the previous color, it'll be changed in GL too.
	 */
	public void setColor(Color color) {
		if (currentColor.equals(color))
			return;

		currentColor = color;
		GL11.glColor4f(
			color.getRed() / 255f,
			color.getGreen() / 255f,
			color.getBlue() / 255f,
			color.getAlpha() / 255f
		);
	}

	/**
	 * Enables the given capabilities if they were not already enabled.
	 */
	public void enable(int... capabilities) {
		for (int capability : capabilities) {
			if (!Boolean.TRUE.equals(this.capabilities.put(capability, true))) {
				ensureActiveState(false);
				GL11.glEnable(capability);
			}
		}
	}

	/**
	 * Disables the given capabilities if they were not already disabled.
	 */
	public void disable(int... capabilities) {
		ensureActiveState(false);
		for (int capability : capabilities)
			if (!Boolean.FALSE.equals(this.capabilities.put(capability, false)))
				GL11.glDisable(capability);
	}

	/**
	 * Draws multiple quads with the given texture
	 */
	public void draw(TextureCutout cutout, Quad... allVertices) {
		Atlas.register(cutout);
		if (Atlas.isOutdated())
			return;

		UV uv = Atlas.getAtlas().getUV(cutout);
		for (Quad quad : allVertices)
			draw(uv, quad);
	}

	/**
	 * Draws a quad with the given texture
	 */
	public void draw(TextureCutout cutout, Quad quad) {
		Atlas.register(cutout);
		if (Atlas.isOutdated())
			return;

		UV uv = Atlas.getAtlas().getUV(cutout);
		draw(uv, quad);
	}

	private void draw(UV uv, Quad quad) {
		quad = quad.translate(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);

		ensureActiveState(true);
		drawVertex(quad.a(), uv.maxU(), uv.maxV());
		drawVertex(quad.b(), uv.minU(), uv.maxV());
		drawVertex(quad.c(), uv.minU(), uv.minV());
		drawVertex(quad.d(), uv.maxU(), uv.minV());
	}

	private void drawVertex(Vec3 pos, double u, double v) {
		glTexCoord2f((float) u, (float) v);
		drawVertex(pos.xCoord, pos.yCoord, pos.zCoord);
	}

	@Override
	public void drawVertex(double x, double y, double z) {
		ensureActiveState(true);
		glVertex3f((float) x, (float) y, (float) z);
	}

}
