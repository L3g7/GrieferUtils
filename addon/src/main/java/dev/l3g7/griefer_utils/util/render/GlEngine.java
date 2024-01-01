/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

import dev.l3g7.griefer_utils.core.misc.Vec3d;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing.Axis;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static net.minecraft.util.EnumFacing.Axis.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * A GL engine.
 */
public class GlEngine {

	private static final TextureManager textureManager = mc().getTextureManager();
	private static final Tessellator tessellator = Tessellator.getInstance();
	private static final WorldRenderer worldrenderer = tessellator.getWorldRenderer();

	private static final Map<Integer, Boolean> capabilities = new HashMap<>();
	private static boolean active = false;
	private static boolean blocksBlurAndMipmapDisabled = false;
	private static boolean isWorldDrawing = false;
	private static Boolean depthMask = null;

	/**
	 * Begins a session.
	 *
	 * @see GlEngine#finish()
	 */
	public static void begin() {
		if (active)
			throw new IllegalStateException("already active!");

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
	public static void finish() {
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

		// Reset caps
		capabilities.forEach((capability, enabled) -> {
			if (enabled) glEnable(capability);
			else glDisable(capability);
		});

		// Reset color
		glColor4f(1, 1, 1, 1);

		// Reset depth mask
		if (depthMask != null)
			glDepthMask(depthMask);

		glPopMatrix();
		active = false;
	}

	public static void translate(float translation) {
		checkActive();
		GlStateManager.translate(translation, translation, translation);
	}

	public static void translate(Vec3d translation) {
		checkActive();
		GlStateManager.translate(translation.x, translation.y, translation.z);
	}

	public static void scale(float scale) {
		checkActive();
		GlStateManager.scale(scale, scale, scale);
	}

	public static void rotate(float angle, Axis axis) {
		checkActive();
		glRotatef(angle, axis == X ? 1 : 0, axis == Y ? 1 : 0, axis == Z ? 1 : 0);
	}

	public static void enable(int... capabilities) {
		checkActive();

		for (int capability : capabilities) {
			GlEngine.capabilities.put(capability, glGetBoolean(capability));
			glEnable(capability);
		}
	}

	public static void disable(int... capabilities) {
		checkActive();

		for (int capability : capabilities) {
			GlEngine.capabilities.put(capability, glGetBoolean(capability));
			glDisable(capability);
		}
	}

	public static void enableDepth() {
		enable(GL_DEPTH_TEST);
		depthMask = glGetBoolean(GL_DEPTH_WRITEMASK);
		glDepthMask(true);
	}

	public static void disableDepth() {
		disable(GL_DEPTH_TEST);
		depthMask = glGetBoolean(GL_DEPTH_WRITEMASK);
		glDepthMask(false);
	}

	public static void color(Color color) {
		color(color, 1);
	}

	/**
	 * @param alpha 0-255
	 */
	public static void color(Color color, float alpha) {
		glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f * alpha);
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
	 * Adds a position to the current WorldRenderer drawing session.
	 */
	public static WorldRenderer pos(Vec3d pos) {
		return worldrenderer.pos((float) pos.x, (float) pos.y, (float) pos.z);
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
