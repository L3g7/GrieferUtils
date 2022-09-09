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
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

import java.util.Stack;

import static net.minecraft.client.renderer.GlStateManager.enableBlend;
import static net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;

/**
 * A GL state engine.
 */
public class GlStateEngine {

	/**
	 * Whether GL14 is available.
	 */
	private static final boolean openGL14;
	static {
		ContextCapabilities capabilities = GLContext.getCapabilities();
		openGL14 = capabilities.OpenGL14 || capabilities.GL_EXT_blend_func_separate;
	}

	/**
	 * The store for gl states.
	 */
	private static final Stack<GLState> stateStack = new Stack<>();

	/**
	 * Stores the current gl state.
	 *
	 * @see GlStateEngine#end()
	 */
	public static void begin() {
		glPushMatrix();
		stateStack.push(new GLState());
	}

	/**
	 * Restores the gl state at the time of the last {@link GlStateEngine#begin()} call.
	 *
	 * @see GlStateEngine#begin()
	 */
	public static void end() {
		glPopMatrix();
		GLState state = stateStack.pop();

		// Reset blend
		if (state.blendEnabled)
			glEnable(GL_BLEND);
		else
			glDisable(GL_BLEND);

		// Reset blendFunc
		tryBlendFuncSeparate(state.srcFactor, state.dstFactor, state.srcFactorAlpha, state.dstFactorAlpha);
	}


	/**
	 * multiplies the current matrix by a translation matrix.
	 */
	public static void translate(float x, float y) {
		GlStateManager.translate(x, y, 1);
	}

	/**
	 * Specifies pixel arithmetic for RGB and, if possible, alpha components.
	 */
	public static void blend(int srcFactor, int dstFactor, int srcFactorAlpha, int dstFactorAlpha) {
		enableBlend();
		tryBlendFuncSeparate(srcFactor, dstFactor, srcFactorAlpha, dstFactorAlpha);
	}

	/**
	 * Multiplies the current matrix by a general scaling matrix.
	 */
	public static void scale(float scale) {
		GlStateManager.scale(scale, scale, scale);
	}

	/**
	 * A stack entry storing the gl state at the time of its creation.
	 */
	private static class GLState {

		private final boolean blendEnabled = glIsEnabled(GL_BLEND);
		private final int srcFactor = glGetInteger(openGL14 ? GL_BLEND_SRC_RGB : GL_BLEND_SRC);
		private final int dstFactor = glGetInteger(openGL14 ? GL_BLEND_DST_RGB : GL_BLEND_DST);
		private final int srcFactorAlpha = openGL14 ? glGetInteger(GL_BLEND_SRC_ALPHA) : 0;
		private final int dstFactorAlpha = openGL14 ? glGetInteger(GL_BLEND_DST_ALPHA) : 0;

	}

}
