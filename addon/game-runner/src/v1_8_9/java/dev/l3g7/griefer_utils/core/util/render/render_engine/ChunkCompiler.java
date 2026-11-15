/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render.render_engine;

import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Function;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.Quad;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.TextureCutout;
import dev.l3g7.griefer_utils.core.util.render.render_engine.data.UV;
import dev.l3g7.griefer_utils.core.util.render.sinks.VertexSink;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.nio.ByteBuffer;

import static java.awt.Color.WHITE;

/**
 * Collects the vertices of {@link RenderObject RenderObjects} into a buffer, which is then uploaded into a VBO.
 * @see CompiledBuffer
 */
public class ChunkCompiler implements VertexSink {

	/**
	 * The initial size of the vertex buffer, in vertices.
	 */
	private static final int INITIAL_CAPACITY = 8192;

	private ByteBuffer buffer = GLAllocation.createDirectByteBuffer(INITIAL_CAPACITY * CompiledBuffer.VERTEX_SIZE);
	private int vertexCount = 0;

	/**
	 * The position of the RenderObject currently being drawing
	 */
	private BlockPos blockPos;
	private Color currentColor = WHITE;
	private Function<Quad, Quad> currentTransformation;

	/**
	 * Prepares the compiler for drawing a new {@link RenderObject}
	 * @param blockPos The RenderObject's positions
	 */
	public void prepare(BlockPos blockPos) {
		setColor(WHITE);
		this.currentTransformation = null;
		this.blockPos = blockPos;
	}

	/**
	 * Discards all previously drawn vertices.
	 */
	void begin() {
		buffer.clear();
		vertexCount = 0;
	}

	/**
	 * @return The amount of vertices drawn since the last call to {@link #begin()}.
	 */
	int getVertexCount() {
		return vertexCount;
	}

	/**
	 * Uploads all vertices drawn since {@link #begin()} into the given VBO.
	 */
	void upload(int vbo) {
		buffer.flip();
		OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, vbo);
		OpenGlHelper.glBufferData(OpenGlHelper.GL_ARRAY_BUFFER, buffer, OpenGlHelper.GL_STATIC_DRAW);
		OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
		buffer.clear();
		vertexCount = 0;
	}

	/**
	 * Sets the current color to a gray color with the given brightness.
	 * @param brightness The color's brightness value, from 0 to 255.
	 * @see #setColor(Color)
	 */
	public void setBrightness(int brightness) {
		setColor(new Color(brightness, brightness, brightness));
	}

	/**
	 * Sets the current color.
	 * Will be reset to white before starting a new RenderObject.
	 */
	public void setColor(Color color) {
		currentColor = color;
	}

	/**
	 * Sets the current transformation, which will be applied to each Quad before rendering.
	 * Will be reset before starting a new RenderObject.
	 */
	public void setTransformation(Function<Quad, Quad> transformation) {
		this.currentTransformation = transformation;
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
		if (this.currentTransformation != null)
			quad = this.currentTransformation.apply(quad);
		quad = quad.translate(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);

		drawVertex(quad.a(), uv.maxU(), uv.maxV());
		drawVertex(quad.b(), uv.minU(), uv.maxV());
		drawVertex(quad.c(), uv.minU(), uv.minV());
		drawVertex(quad.d(), uv.maxU(), uv.minV());
	}

	private void drawVertex(Vec3 pos, double u, double v) {
		putVertex(pos.xCoord, pos.yCoord, pos.zCoord, (float) u, (float) v);
	}

	@Override
	public void drawVertex(double x, double y, double z) {
		putVertex(x, y, z, 0, 0);
	}

	private void putVertex(double x, double y, double z, float u, float v) {
		ensureCapacity();

		buffer.putFloat((float) x);
		buffer.putFloat((float) y);
		buffer.putFloat((float) z);
		buffer.putFloat(u);
		buffer.putFloat(v);
		buffer.put((byte) currentColor.getRed());
		buffer.put((byte) currentColor.getGreen());
		buffer.put((byte) currentColor.getBlue());
		buffer.put((byte) currentColor.getAlpha());

		vertexCount++;
	}

	/**
	 * Grows the buffer if there's no space for another vertex.
	 */
	private void ensureCapacity() {
		if (buffer.remaining() >= CompiledBuffer.VERTEX_SIZE)
			return;

		ByteBuffer newBuffer = GLAllocation.createDirectByteBuffer(buffer.capacity() * 2);
		buffer.flip();
		newBuffer.put(buffer);
		buffer = newBuffer;
	}

}
