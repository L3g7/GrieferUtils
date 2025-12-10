/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.features.uncategorized.debug.wiki;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class ItemRenderer {

	public final int size;
	private final AnimatedGifEncoder encoder = new AnimatedGifEncoder();
	public final ItemStack stack;
	private float oldZLevel;
	public List<ByteBuffer> buffers = new ArrayList<>(320);

	public ItemRenderer(ItemStack stack, int size) {
		this.stack = stack.copy();
		this.size = size;
		ImageUtil.size = size;
		Color c = new Color(0, 0, 255);
		encoder.setBackground(c);
		encoder.setTransparent(c);
	}

	public void finish() {
		encoder.start(new File(Minecraft.getMinecraft().mcDataDir, "rendered_enchantments/" + stack.getDisplayName().toLowerCase().replace(' ', '_') + ".gif").getAbsolutePath());
		encoder.setFrameRate(25);
		for (ByteBuffer buffer : buffers) {
			BufferedImage img = ImageUtil.toImage(buffer);
			img = ImageUtil.createFlipped(img);
			encoder.addFrame(img);
		}
		encoder.finish();
	}

	public BufferedImage grabFrame() {
		GlStateManager.pushMatrix();
		setUpRenderState();

		GlStateManager.pushMatrix();
		GlStateManager.clearColor(0, 0, 0, 0);
		GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		mc().getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
		mc().getRenderItem().renderItemOverlayIntoGUI(mc().fontRendererObj, stack, 0, 0, null);

		GlStateManager.popMatrix();
		ByteBuffer buf = ImageUtil.readPixels();
		tearDownRenderState();
		GlStateManager.popMatrix();

		return ImageUtil.createFlipped(ImageUtil.toImage(buf));
	}

	public void addFrame() {
		GlStateManager.pushMatrix();
		setUpRenderState();

		GlStateManager.pushMatrix();
		GlStateManager.clearColor(0, 0, 0, 0);
		GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		mc().getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
		GlStateManager.popMatrix();
		buffers.add(ImageUtil.readPixels());
		tearDownRenderState();
		GlStateManager.popMatrix();
	}

	private void setUpRenderState() {
		ScaledResolution res = new ScaledResolution(mc());
		mc().entityRenderer.setupOverlayRendering();
		RenderHelper.enableGUIStandardItemLighting();
		float scale = size / (16f * res.getScaleFactor());
		GlStateManager.translate(0, 0, -(scale * 100));
		GlStateManager.scale(scale, scale, scale);
		oldZLevel = mc().getRenderItem().zLevel;
		mc().getRenderItem().zLevel = -50;

		GlStateManager.enableRescaleNormal();
		GlStateManager.enableColorMaterial();
		GlStateManager.enableDepth();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableAlpha();
	}

	private void tearDownRenderState() {
		RenderHelper.disableStandardItemLighting();

		GlStateManager.disableRescaleNormal();
		GlStateManager.disableColorMaterial();
		GlStateManager.disableDepth();
		GlStateManager.disableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.disableLighting();

		Minecraft.getMinecraft().getRenderItem().zLevel = oldZLevel;
	}

}
