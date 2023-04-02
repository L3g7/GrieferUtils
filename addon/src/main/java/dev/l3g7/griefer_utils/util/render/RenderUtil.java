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

package dev.l3g7.griefer_utils.util.render;

import dev.l3g7.griefer_utils.core.misc.Vec3d;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.events.render.RenderToolTipEvent;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.function.BiConsumer;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.pos;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;
import static dev.l3g7.griefer_utils.util.render.GlEngine.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * A utility class for rendering stuff.
 */
public class RenderUtil {

	private static final RenderItem itemRender = mc().getRenderItem();

	/**
	 * Renders an item with a given color tint.
	 */
	public static void renderItem(ItemStack stack, int x, int y, int color) {
		IBakedModel model = itemRender.getItemModelMesher().getItemModel(stack);

		begin();
		disableBlocksBlurAndMipmap();

		// Transform
		Reflection.invoke(itemRender, "setupGuiTransform", x, y, model.isGui3d());
		model = ForgeHooksClient.handleCameraTransforms(model, TransformType.GUI);
		scale(0.5F);
		translate(-0.5F);

		// Draw item
		WorldRenderer worldrenderer = beginWorldDrawing(GL_QUADS, DefaultVertexFormats.ITEM);
		Reflection.invoke(itemRender, "renderQuads", worldrenderer, model.getGeneralQuads(), color, stack);

		finish();
	}

	public static void drawBoxOutlines(AxisAlignedBB bb, Color color, float width) {
		drawBoxOutlines((float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ, color, width);
	}

	public static void drawBoxOutlines(float startX, float startY, float startZ, float endX, float endY, float endZ, Color color, float width) {
		// Lower rectangle
		drawLine(startX,	startY,	startZ,	startX,	startY,	endZ,	color,	width);
		drawLine(startX,	startY,	startZ,	endX,	startY,	startZ,	color,	width);
		drawLine(endX,		startY,	startZ,	endX,	startY,	endZ,	color,	width);
		drawLine(startX,	startY,	endZ,	endX,	startY,	endZ,	color,	width);

		// upper rectangle
		drawLine(startX,	endY,	startZ,	startX,	endY,	endZ,	color,	width);
		drawLine(startX,	endY,	startZ,	endX,	endY,	startZ,	color,	width);
		drawLine(endX,		endY,	startZ,	endX,	endY,	endZ,	color,	width);
		drawLine(startX,	endY,	endZ,	endX,	endY,	endZ,	color,	width);

		// connecting lines
		drawLine(startX,	startY,	startZ,	startX,	endY,	startZ,	color,	width);
		drawLine(startX,	startY,	endZ,	startX,	endY,	endZ,	color,	width);
		drawLine(endX,		startY,	startZ,	endX,	endY,	startZ,	color,	width);
		drawLine(endX,		startY,	endZ,	endX,	endY,	endZ,	color,	width);
	}

	public static void drawLine(BlockPos start, BlockPos end, Color color, float width) {
		drawLine(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ(), color, width);
	}

	/**
	 * Based on <a href="https://github.com/CCBlueX/LiquidBounce/blob/5419a2894b4665b7695d0443180275a70f13607a/src/main/java/net/ccbluex/liquidbounce/utils/render/RenderUtils.java#L82">LiquidBounce's RenderUtils#drawBlockBox</a>
	 */
	public static void drawLine(float startX, float startY, float startZ, float endX, float endY, float endZ, Color color, float width) {
		Entity entity = mc().getRenderViewEntity();

		// Get cam pos
		Vec3d prevPos = new Vec3d(entity.prevPosX, entity.prevPosY, entity.prevPosZ);

		Vec3d cam = prevPos.add(pos(entity).subtract(prevPos).scale(partialTicks()));

		// Update line width
		GL11.glLineWidth(width);
		GlStateManager.disableTexture2D();

		// Draw lines
		GlEngine.begin();
		WorldRenderer buf = GlEngine.beginWorldDrawing(GL_LINES, DefaultVertexFormats.POSITION);
		GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

		buf.pos(startX - cam.x, startY - cam.y, startZ - cam.z).endVertex();
		buf.pos(endX - cam.x, endY - cam.y, endZ - cam.z).endVertex();

		GlEngine.finish();

		// Reset line width
		GlStateManager.enableTexture2D();
	}

	public static void drawFilledBox(AxisAlignedBB bb, Color color) {
		Entity entity = mc().getRenderViewEntity();
		Vec3d prevPos = new Vec3d(entity.prevPosX, entity.prevPosY, entity.prevPosZ);
		Vec3d cam = prevPos.add(pos(entity).subtract(prevPos).scale(partialTicks()));
		bb = bb.offset(-cam.x, -cam.y, -cam.z);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION);

		GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
		GlStateManager.enableBlend();
		GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableTexture2D();

		worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
		worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
		worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
		worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
		worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
		worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
		worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
		worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
		worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
		worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
		worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
		worldRenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
		worldRenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
		worldRenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
		tessellator.draw();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
	}

	public static void drawGradientRect(double left, double top, double right, double bottom, double zLevel, int startColor, int endColor) {
		float f = (startColor >> 24 & 255) / 255f;
		float f1 = (startColor >> 16 & 255) / 255f;
		float f2 = (startColor >> 8 & 255) / 255f;
		float f3 = (startColor & 255) / 255f;
		float f4 = (endColor >> 24 & 255) / 255f;
		float f5 = (endColor >> 16 & 255) / 255f;
		float f6 = (endColor >> 8 & 255) / 255f;
		float f7 = (endColor & 255) / 255f;

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GlStateManager.shadeModel(GL_SMOOTH);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		worldrenderer.pos(right, top, zLevel).color(f1, f2, f3, f).endVertex();
		worldrenderer.pos(left, top, zLevel).color(f1, f2, f3, f).endVertex();
		worldrenderer.pos(left, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
		worldrenderer.pos(right, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
		tessellator.draw();

		GlStateManager.shadeModel(GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	public static void renderToolTipWithLeftPadding(RenderToolTipEvent event, float padding, BiConsumer<Float, Float> paddingContentRenderer) {
		List<String> textLines = event.stack.getTooltip(player(), settings().advancedItemTooltips);
		for (int i = 0; i < textLines.size(); ++i)
			textLines.set(i, (i == 0 ? event.stack.getRarity().rarityColor : "§7") + textLines.get(i));

		if (textLines.isEmpty())
			return;

		event.setCanceled(true);

		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		GlStateManager.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();

		int maxLineWidth = 0;
		for (String line : textLines) {
			int width = drawUtils().getStringWidth(line);
			if (width > maxLineWidth)
				maxLineWidth = width;
		}

		float mouseOffset = padding + 12;
		float drawX = event.x + mouseOffset;
		float drawY = event.y - 12;
		float defaultHeight = textLines.size() == 1 ? 14.5f : 8;
		if (textLines.size() > 1)
			defaultHeight += 2 + (textLines.size() - 1) * 10;
		defaultHeight = Math.max(padding, defaultHeight);

		if (drawX + maxLineWidth > event.screen.width)
			drawX -= mouseOffset + maxLineWidth;
		if (drawY + defaultHeight + 6 > event.screen.height)
			drawY = event.screen.height - defaultHeight - 6;

		int color = 0xF0100010;
		float y = drawY + defaultHeight + 3;
		float x = drawX + maxLineWidth + 3;
		float paddedX = drawX - 5 - padding;
		drawY -= 3;
		drawGradientRect(paddedX    , drawY - 1, x        , drawY, 0, color, color);
		drawGradientRect(paddedX    , y        , x        , y + 1, 0, color, color);
		drawGradientRect(drawX - 3  , drawY    , x        , y    , 0, color, color);
		drawGradientRect(x          , drawY    , x + 1    , y    , 0, color, color);
		drawGradientRect(paddedX - 1, drawY    , drawX - 3, y    , 0, color, color);

		int startColor = 0x505000FF;
		int endColor = (startColor & 0xFEFEFE) >> 1 | (startColor & 0xFF000000);
		drawGradientRect(paddedX, drawY + 1, paddedX + 1, y - 1    , 0, startColor, endColor);
		drawGradientRect(x - 1  , drawY + 1, x          , y - 1    , 0, startColor, endColor);
		drawGradientRect(paddedX, drawY    , x          , drawY + 1, 0, startColor, startColor);
		drawGradientRect(paddedX, y - 1    , x          , y        , 0, endColor  , endColor);
		drawY += 3;

		float rectTop = drawY;
		for (int i = 0; i < textLines.size(); ++i) {
			String line = textLines.get(i);
			drawUtils().drawStringWithShadow(line, drawX, drawY, -1);
			if (i == 0)
				drawY += 2;
			drawY += 10;
		}

		GlStateManager.enableBlend();
		GlStateManager.enableDepth();
		GlStateManager.enableRescaleNormal();
		GlStateManager.pushMatrix();
		paddingContentRenderer.accept(paddedX + 2, rectTop);
		GlStateManager.popMatrix();
	}

}
