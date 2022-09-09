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

package dev.l3g7.griefer_utils.util;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.StaticImport.mc;
import static dev.l3g7.griefer_utils.util.render.GlStateEngine.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * description missing.
 */
public class RenderUtil {

	private static final FontRenderer iconFontRenderer = new FontRenderer(mc().gameSettings, new ResourceLocation("griefer_utils/icon_font.png"), mc().renderEngine, false);

	public static final char[] ARMOR_ICONS = new char[] {'\u2502', '\u2593', '\u2592', '\u2591'};

	static {
		((IReloadableResourceManager) mc().getResourceManager()).registerReloadListener(iconFontRenderer);
	}

	public static void renderTitle(String title, float scale, int color, boolean subTitle) {
		ScaledResolution res = new ScaledResolution(mc());
		begin();


		translate(res.getScaledWidth() / 2f, res.getScaledHeight() / 2f);
		blend(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);

		begin();
		scale(scale);
		iconFontRenderer.drawString(title, -iconFontRenderer.getStringWidth(title) / 2f, subTitle ? 5 : -10, color, true);
		end();

		end();
	}

}
