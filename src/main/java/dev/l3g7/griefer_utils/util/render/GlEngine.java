/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.util.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.StaticImport.mc;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;

/**
 * A GL engine.
 */
public class GlEngine {

	private static final TextureManager textureManager = mc().getTextureManager();
	private static final Tessellator tessellator = Tessellator.getInstance();
	private static final WorldRenderer worldrenderer = tessellator.getWorldRenderer();

	private static boolean active = false;
	private static boolean blocksBlurAndMipmapDisabled = false;
	private static boolean isWorldDrawing = false;

	/**
	 * Begins a session.
	 *
	 * @see GlEngine#end()
	 */
	public static void begin() {
		if (active)
			throw new IllegalStateException("Session already active!");

		glPushMatrix();
		active = true;
		blocksBlurAndMipmapDisabled = false;
		isWorldDrawing = false;
	}

	/**
	 * Ends the session and finishes uncompleted actions.
	 *
	 * @see GlEngine#begin()
	 */
	public static void end() {
		checkActive();

		// Reset blocksBlurMipmap
		if (blocksBlurAndMipmapDisabled) {
			textureManager.bindTexture(TextureMap.locationBlocksTexture);
			textureManager.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
		}

		// End world drawing
		if (isWorldDrawing) {
			endWorldDrawing();
		}

		glPopMatrix();
		active = false;
	}

	/**
	 * Multiplies the current matrix by a translation matrix.
	 */
	public static void translate(float x, float y, float z) {
		checkActive();
		GlStateManager.translate(x, y, z);
	}

	/**
	 * Multiplies the current matrix by a general scaling matrix.
	 */
	public static void scale(float scale) {
		checkActive();
		GlStateManager.scale(scale, scale, scale);
	}

	/**
	 * Disables the blur and mipmap for the blocks atlas.
	 *
	 * @see TextureMap#locationBlocksTexture
	 */
	public static void disableBlocksBlurAndMipmap() {
		checkActive();

		textureManager.bindTexture(TextureMap.locationBlocksTexture);
		textureManager.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
		blocksBlurAndMipmapDisabled = true;
	}

	/**
	 * Begins a WorldRenderer drawing session.
	 */
	public static WorldRenderer beginWorldDrawing(int glMode, VertexFormat format) {
		checkActive();
		if (isWorldDrawing)
			endWorldDrawing();

		worldrenderer.begin(glMode, format);

		isWorldDrawing = true;
		return worldrenderer;
	}

	/**
	 * Ends a WorldRenderer drawing session.
	 */
	public static void endWorldDrawing() {
		checkActive();
		if (!isWorldDrawing)
			throw new IllegalStateException("World drawing is not active!");

		tessellator.draw();
		isWorldDrawing = false;
	}

	/**
	 * Checks whether a session is active.
	 */
	private static void checkActive() {
		if (!active)
			throw new IllegalStateException("Session not active!");
	}

}
