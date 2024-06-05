/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.util.render;

import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event.event_bus.Priority;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.TickEvent.RenderTickEvent;
import dev.l3g7.griefer_utils.core.events.render.RenderToolTipEvent;
import dev.l3g7.griefer_utils.core.misc.Vec3d;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import net.minecraft.client.gui.ScaledResolution;
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
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.pos;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static dev.l3g7.griefer_utils.core.util.render.GlEngine.*;
import static net.minecraft.util.EnumFacing.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * A utility class for rendering stuff.
 */
public class RenderUtil {

	private static final RenderItem itemRender = mc().getRenderItem();
	private static ScaledResolution scaledResolution = null;

	@EventListener(priority = Priority.HIGH)
	private static void onRenderTick(RenderTickEvent event) {
		scaledResolution = new ScaledResolution(mc());
	}

	public static boolean shouldBeCulled(int minY, int maxY) {
		return maxY < 0 || RenderUtil.scaledResolution.getScaledHeight() < minY;
	}

	/**
	 * Renders an item with a given color tint.
	 */
	public static void renderItem(ItemStack stack, int x, int y, int color) {
		IBakedModel model = itemRender.getItemModelMesher().getItemModel(stack);

		begin();
		disableBlocksBlurAndMipmap();

		// Transform
		Reflection.invoke(itemRender, "setupGuiTransform", x, y, model.isGui3d());
		model.getItemCameraTransforms().applyTransform(TransformType.GUI);
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

	public static void drawLine(float startX, float startY, float startZ, float endX, float endY, float endZ, Color color, float width) {
		Entity entity = mc().getRenderViewEntity();

		// Get cam pos
		Vec3d prevPos = new Vec3d(entity.prevPosX, entity.prevPosY, entity.prevPosZ);

		Vec3d cam = prevPos.add(pos(entity).subtract(prevPos).scale(partialTicks()));

		// Update line width
		GL11.glLineWidth(width);
		GlStateManager.disableTexture2D();

		// Draw lines
		begin();
		WorldRenderer buf = GlEngine.beginWorldDrawing(GL_LINES, DefaultVertexFormats.POSITION);
		GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

		buf.pos(startX - cam.x, startY - cam.y, startZ - cam.z).endVertex();
		buf.pos(endX - cam.x, endY - cam.y, endZ - cam.z).endVertex();

		GlEngine.finish();

		// Reset line width
		GlStateManager.enableTexture2D();
	}

	public static void drawFilledBox(AxisAlignedBB bb, Color color, boolean drawInside) {
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

		Object blendState = Reflection.get(GlStateManager.class, "blendState");
		int originalSrcFactor = Reflection.get(blendState, "srcFactor");
		int originalDstFactor = Reflection.get(blendState, "dstFactor");
		GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		drawFilledBoxWhenRenderingStarted(bb, drawInside);

		tessellator.draw();
		GlStateManager.blendFunc(originalSrcFactor, originalDstFactor);
		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
		GL11.glColor4f(1f, 1f, 1f, 1f);
	}

	public static void drawFilledBoxWhenRenderingStarted(AxisAlignedBB bb, boolean drawInside) {
		drawFace(bb, UP, drawInside);
		drawFace(bb, DOWN, drawInside);
		drawFace(bb, NORTH, drawInside);
		drawFace(bb, SOUTH, drawInside);
		drawFace(bb, WEST, drawInside);
		drawFace(bb, EAST, drawInside);
	}

	public static void drawFace(AxisAlignedBB bb, EnumFacing face, boolean bothSides) {
		WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
		if (bothSides) {
			bb = new AxisAlignedBB(
				face == EAST ? bb.maxX : bb.minX,
				face == UP ? bb.maxY : bb.minY,
				face == SOUTH ? bb.maxZ : bb.minZ,
				face == WEST ? bb.minX : bb.maxX,
				face == DOWN ? bb.minY : bb.maxY,
				face == NORTH ? bb.minZ : bb.maxZ
			);
		}

		if (face.getAxis() == Axis.Y) {
			if (face == UP || bothSides) {
				wr.pos(bb.minX, bb.maxY, bb.maxZ); wr.endVertex();
				wr.pos(bb.maxX, bb.maxY, bb.maxZ); wr.endVertex();
				wr.pos(bb.maxX, bb.maxY, bb.minZ); wr.endVertex();
				wr.pos(bb.minX, bb.maxY, bb.minZ); wr.endVertex();
			}

			if (face == DOWN || bothSides) {
				wr.pos(bb.maxX, bb.minY, bb.maxZ); wr.endVertex();
				wr.pos(bb.minX, bb.minY, bb.maxZ); wr.endVertex();
				wr.pos(bb.minX, bb.minY, bb.minZ); wr.endVertex();
				wr.pos(bb.maxX, bb.minY, bb.minZ); wr.endVertex();
			}
			return;
		}

		if (face.getAxis() == Axis.X) {
			if (face == EAST || bothSides) {
				wr.pos(bb.maxX, bb.maxY, bb.maxZ); wr.endVertex();
				wr.pos(bb.maxX, bb.minY, bb.maxZ); wr.endVertex();
				wr.pos(bb.maxX, bb.minY, bb.minZ); wr.endVertex();
				wr.pos(bb.maxX, bb.maxY, bb.minZ); wr.endVertex();
			}

			if (face == WEST || bothSides) {
				wr.pos(bb.minX, bb.minY, bb.maxZ); wr.endVertex();
				wr.pos(bb.minX, bb.maxY, bb.maxZ); wr.endVertex();
				wr.pos(bb.minX, bb.maxY, bb.minZ); wr.endVertex();
				wr.pos(bb.minX, bb.minY, bb.minZ); wr.endVertex();
			}
			return;
		}

		if (face == SOUTH || bothSides) {
			wr.pos(bb.maxX, bb.maxY, bb.maxZ); wr.endVertex();
			wr.pos(bb.minX, bb.maxY, bb.maxZ); wr.endVertex();
			wr.pos(bb.minX, bb.minY, bb.maxZ); wr.endVertex();
			wr.pos(bb.maxX, bb.minY, bb.maxZ); wr.endVertex();
		}

		if (face == NORTH || bothSides) {
			wr.pos(bb.minX, bb.maxY, bb.minZ); wr.endVertex();
			wr.pos(bb.maxX, bb.maxY, bb.minZ); wr.endVertex();
			wr.pos(bb.maxX, bb.minY, bb.minZ); wr.endVertex();
			wr.pos(bb.minX, bb.minY, bb.minZ); wr.endVertex();
		}
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

		event.cancel();
		renderToolTipWithPadding(textLines, event.x, event.y, event.screen.width, event.screen.height, padding, 0, padding, paddingContentRenderer);
	}

	public static void renderToolTipWithPadding(List<String> textLines, int mouseX, int mouseY, int width, int height, float leftPadding, float otherPadding, float minHeight, BiConsumer<Float, Float> paddingContentRenderer) {
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableBlend();
		GlStateManager.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();

		int maxLineWidth = 0;
		for (String line : textLines) {
			int lineWidth = DrawUtils.getStringWidth(line);
			if (lineWidth > maxLineWidth)
				maxLineWidth = lineWidth;
		}

		float mouseOffset = leftPadding + 12;
		float drawX = mouseX + mouseOffset;
		float drawY = mouseY - 12;
		float defaultHeight = textLines.size() == 1 ? 14.5f : 8;
		if (textLines.size() > 1)
			defaultHeight += 2 + (textLines.size() - 1) * 10;
		defaultHeight = Math.max(minHeight, defaultHeight);

		if (drawX + maxLineWidth > width)
			drawX -= mouseOffset + maxLineWidth;
		if (drawY + defaultHeight + 6 > height)
			drawY = height - defaultHeight - 6;

		int color = 0xF0100010;
		float y = drawY + defaultHeight + 3 + otherPadding * 2;
		float x = drawX + maxLineWidth + 3 + otherPadding;
		float paddedX = drawX - 5 - leftPadding;
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
		drawY += 3 + otherPadding;

		float rectTop = drawY;
		for (int i = 0; i < textLines.size(); ++i) {
			String line = textLines.get(i);
			DrawUtils.drawStringWithShadow(line, drawX, drawY, -1);
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

	public static Pair<Integer, Integer> getTooltipTranslation(List<String> textLines, int mouseX, int mouseY, int width, int height) {
		if (textLines.isEmpty())
			throw new IllegalArgumentException("textLines can't be empty!");

		int tooltipTextWidth = 0;

		for (String textLine : textLines) {
			int textLineWidth = mc().fontRendererObj.getStringWidth(textLine);

			if (textLineWidth > tooltipTextWidth) {
				tooltipTextWidth = textLineWidth;
			}
		}

		boolean needsWrap = false;

		int titleLinesCount = 1;
		int tooltipX = mouseX + 12;
		if (tooltipX + tooltipTextWidth + 4 > width) {
			tooltipX = mouseX - 16 - tooltipTextWidth;
			if (tooltipX < 4) { // if the tooltip doesn't fit on the screen
				if (mouseX > width / 2)
					tooltipTextWidth = mouseX - 12 - 8;
				else
					tooltipTextWidth = width - 16 - mouseX;
				needsWrap = true;
			}
		}

		if (needsWrap) {
			int wrappedTooltipWidth = 0;
			List<String> wrappedTextLines = new ArrayList<>();
			for (int i = 0; i < textLines.size(); i++) {
				String textLine = textLines.get(i);
				List<String> wrappedLine = mc().fontRendererObj.listFormattedStringToWidth(textLine, tooltipTextWidth);
				if (i == 0) {
					titleLinesCount = wrappedLine.size();
				}

				for (String line : wrappedLine) {
					int lineWidth = mc().fontRendererObj.getStringWidth(line);
					if (lineWidth > wrappedTooltipWidth) {
						wrappedTooltipWidth = lineWidth;
					}
					wrappedTextLines.add(line);
				}
			}
			tooltipTextWidth = wrappedTooltipWidth;
			textLines = wrappedTextLines;

			if (mouseX > width / 2)
				tooltipX = mouseX - 16 - tooltipTextWidth;
			else
				tooltipX = mouseX + 12;
		}

		int tooltipY = mouseY - 12;
		int tooltipHeight = 8;

		if (textLines.size() > 1) {
			tooltipHeight += (textLines.size() - 1) * 10;
			if (textLines.size() > titleLinesCount)
				tooltipHeight += 2; // gap between title lines and next lines
		}

		if (tooltipY + tooltipHeight + 6 > height)
			tooltipY = height - tooltipHeight - 6;

		return Pair.of(tooltipX, tooltipY);
	}

}
