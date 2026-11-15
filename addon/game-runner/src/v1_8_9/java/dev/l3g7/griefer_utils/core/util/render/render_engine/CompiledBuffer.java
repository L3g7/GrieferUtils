/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render.render_engine;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;

import static org.lwjgl.opengl.GL11.*;

/**
 * A VBO containing the compiled quads of one {@link dev.l3g7.griefer_utils.core.util.render.render_engine.data.RenderType RenderType}.<br>
 * <br>
 * Vertex format:
 * <ul>
 *     <li>position: 3 floats</li>
 *     <li>texture coordinates: 2 floats</li>
 *     <li>color: 4 unsigned bytes (RGBA)</li>
 * </ul>
 * Should only be used internally by the render engine.
 */
record CompiledBuffer(int vbo, int[] firstVertexForStep, int[] vertexCount) {

	static final int VERTEX_SIZE = 3 * Float.BYTES + 2 * Float.BYTES + 4 * Byte.BYTES;
	private static final int UV_OFFSET = 3 * Float.BYTES;
	private static final int COLOR_OFFSET = UV_OFFSET + 2 * Float.BYTES;

	/**
	 * Enables the client states required for drawing CompiledBuffers.
	 * Must be called before {@link #draw(int)}.
	 */
	static void beginDrawing() {
		glEnableClientState(GL_VERTEX_ARRAY);
		OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
		glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		glEnableClientState(GL_COLOR_ARRAY);
	}

	/**
	 * Restores the client states changed by {@link #beginDrawing()} and {@link #draw(int)}.
	 */
	static void endDrawing() {
		OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
		glDisableClientState(GL_VERTEX_ARRAY);
		OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
		glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		glDisableClientState(GL_COLOR_ARRAY);
		GlStateManager.resetColor();
	}

	/**
	 * Draws the quads of the given step.
	 */
	void draw(int step) {
		OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, vbo);
		glVertexPointer(3, GL_FLOAT, VERTEX_SIZE, 0L);
		glTexCoordPointer(2, GL_FLOAT, VERTEX_SIZE, UV_OFFSET);
		glColorPointer(4, GL_UNSIGNED_BYTE, VERTEX_SIZE, COLOR_OFFSET);
		glDrawArrays(GL_QUADS, firstVertexForStep[step], vertexCount[step]);
	}

	/**
	 * Deletes the VBO.
	 */
	void delete() {
		OpenGlHelper.glDeleteBuffers(vbo);
	}

}
