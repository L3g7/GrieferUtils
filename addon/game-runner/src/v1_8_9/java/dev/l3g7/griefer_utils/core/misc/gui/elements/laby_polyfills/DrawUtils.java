/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills;

import com.mojang.authlib.GameProfile;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.misc.Pair;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.ModelHumanoidHead;
import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

/**
 * Polyfill for LabyMod 3's DrawUtils and ModColors.
 * NOTE: remove?
 */
@SuppressWarnings("DuplicatedCode")
public class DrawUtils {

	public static float zLevel;

	public static String createColors(String string) {
		return string.replaceAll("(?i)&([a-z0-9])", "§$1");
	}

	public static String removeColor(String string) {
		return string.replaceAll("§[a-z0-9]", "");
	}

	public static void drawRightString(String text, double x, double y, double size) {
		GL11.glPushMatrix();
		GL11.glScaled(size, size, size);
		drawString(text, x / size - (double)getStringWidth(text), y / size);
		GL11.glPopMatrix();
	}

	public static void drawGradientShadowBottom(double y, double left, double right) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();
		int i1 = 4;
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
		GlStateManager.disableAlpha();
		GlStateManager.shadeModel(7425);
		GlStateManager.enableTexture2D();
		mc().getTextureManager().bindTexture(Gui.optionsBackground);
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		worldrenderer.pos(left, y, 0.0).tex(0.0, 1.0).color(0, 0, 0, 255).endVertex();
		worldrenderer.pos(right, y, 0.0).tex(1.0, 1.0).color(0, 0, 0, 255).endVertex();
		worldrenderer.pos(right, y - (double)i1, 0.0).tex(1.0, 0.0).color(0, 0, 0, 0).endVertex();
		worldrenderer.pos(left, y - (double)i1, 0.0).tex(0.0, 0.0).color(0, 0, 0, 0).endVertex();
		tessellator.draw();
		GlStateManager.shadeModel(7424);
		GlStateManager.enableAlpha();
		GlStateManager.disableBlend();
	}

	public static void drawGradientShadowTop(double y, double left, double right) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();
		int i1 = 4;
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
		GlStateManager.disableAlpha();
		GlStateManager.shadeModel(7425);
		GlStateManager.enableTexture2D();
		mc().getTextureManager().bindTexture(Gui.optionsBackground);
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		worldrenderer.pos(left, y + (double)i1, 0.0).tex(0.0, 1.0).color(0, 0, 0, 0).endVertex();
		worldrenderer.pos(right, y + (double)i1, 0.0).tex(1.0, 1.0).color(0, 0, 0, 0).endVertex();
		worldrenderer.pos(right, y, 0.0).tex(1.0, 0.0).color(0, 0, 0, 255).endVertex();
		worldrenderer.pos(left, y, 0.0).tex(0.0, 0.0).color(0, 0, 0, 255).endVertex();
		tessellator.draw();
		GlStateManager.shadeModel(7424);
		GlStateManager.enableAlpha();
		GlStateManager.disableBlend();
	}

	public static void drawOverlayBackground(int startY, int endY) {
		int endAlpha = 255;
		int startAlpha = 255;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();
		mc().getTextureManager().bindTexture(Gui.optionsBackground);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		worldrenderer.pos(0.0, endY, 0.0).tex(0.0, (float)endY / 32.0F).color(64, 64, 64, endAlpha).endVertex();
		worldrenderer.pos(getWidth(), endY, 0.0).tex((float)getWidth() / 32.0F, (float)endY / 32.0F).color(64, 64, 64, endAlpha).endVertex();
		worldrenderer.pos(getWidth(), startY, 0.0).tex((float)getWidth() / 32.0F, (float)startY / 32.0F).color(64, 64, 64, startAlpha).endVertex();
		worldrenderer.pos(0.0, startY, 0.0).tex(0.0, (float)startY / 32.0F).color(64, 64, 64, startAlpha).endVertex();
		tessellator.draw();
	}
	public static void drawRectBorder(double left, double top, double right, double bottom, int color, double thickness) {
		drawRect(left + thickness, top, right - thickness, top + thickness, color);
		drawRect(right - thickness, top, right, bottom, color);
		drawRect(left + thickness, bottom - thickness, right - thickness, bottom, color);
		drawRect(left, top, left + thickness, bottom, color);
	}

	public static void renderItemIntoGUI(ItemStack stack, double x, double y) {
		TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
		IBakedModel ibakedmodel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
		GlStateManager.pushMatrix();
		textureManager.bindTexture(TextureMap.locationBlocksTexture);
		textureManager.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		setupGuiTransform(x, y, ibakedmodel.isGui3d());
		ibakedmodel.getItemCameraTransforms().applyTransform(ItemCameraTransforms.TransformType.GUI);
		Minecraft.getMinecraft().getRenderItem().renderItem(stack, ibakedmodel);
		GlStateManager.disableAlpha();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		GlStateManager.popMatrix();
		textureManager.bindTexture(TextureMap.locationBlocksTexture);
		textureManager.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
	}
	public static void setupGuiTransform(double xPosition, double yPosition, boolean isGui3d) {
		GlStateManager.translate((float)xPosition, (float)yPosition, 100.0F + zLevel);
		GlStateManager.translate(8.0F, 8.0F, 0.0F);
		GlStateManager.scale(1.0F, 1.0F, -1.0F);
		GlStateManager.scale(0.5F, 0.5F, 0.5F);
		if (isGui3d) {
			GlStateManager.scale(40.0F, 40.0F, 40.0F);
			GlStateManager.rotate(210.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.enableLighting();
		} else {
			GlStateManager.scale(64.0F, 64.0F, 64.0F);
			GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.disableLighting();
		}

	}

	 public static void drawItem(ItemStack item, double xPosition, double yPosition, String value) {
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.enableCull();
		if (item.hasEffect()) {
			GlStateManager.enableDepth();
			renderItemIntoGUI(item, xPosition, yPosition);
			GlStateManager.disableDepth();
		} else {
			renderItemIntoGUI(item, xPosition, yPosition);
		}

		renderItemOverlayIntoGUI(item, xPosition, yPosition, value);
		GlStateManager.disableDepth();
		GlStateManager.disableLighting();
	}

	public static void renderItemOverlayIntoGUI(ItemStack stack, double xPosition, double yPosition, String text) {
		if (stack != null) {
			if (stack.stackSize != 1 || text != null) {
				String s = text == null ? String.valueOf(stack.stackSize) : text;
				if (text == null && stack.stackSize < 1) {
					s = EnumChatFormatting.RED + String.valueOf(stack.stackSize);
				}

				GlStateManager.disableLighting();
				GlStateManager.disableDepth();
				GlStateManager.disableBlend();
				drawString(s, xPosition + 19.0 - 2.0 - (double)getStringWidth(s), yPosition + 6.0 + 3.0);
				GlStateManager.enableLighting();
				GlStateManager.enableDepth();
			}

			if (stack.isItemDamaged()) {
				int j = (int)Math.round(13.0 - (double)stack.getItemDamage() * 13.0 / (double)stack.getMaxDamage());
				int i = (int)Math.round(255.0 - (double)stack.getItemDamage() * 255.0 / (double)stack.getMaxDamage());
				GlStateManager.disableLighting();
				GlStateManager.disableDepth();
				GlStateManager.disableTexture2D();
				GlStateManager.disableAlpha();
				GlStateManager.disableBlend();
				drawItemTexture(xPosition + 2.0, yPosition + 13.0, 13.0, 2.0, 0, 0, 0, 255);
				drawItemTexture(xPosition + 2.0, yPosition + 13.0, 12.0, 1.0, (255 - i) / 4, 64, 0, 255);
				drawItemTexture(xPosition + 2.0, yPosition + 13.0, j, 1.0, 255 - i, i, 0, 255);
				GlStateManager.enableBlend();
				GlStateManager.enableAlpha();
				GlStateManager.enableTexture2D();
				GlStateManager.enableLighting();
				GlStateManager.enableDepth();
			}
		}

	}
	public static void drawItemTexture(double x, double y, double z, double offset, int red, int green, int blue, int alpha) {
		WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
		worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		worldRenderer.pos(x + 0.0, y + 0.0, 0.0).color(red, green, blue, alpha).endVertex();
		worldRenderer.pos(x + 0.0, y + offset, 0.0).color(red, green, blue, alpha).endVertex();
		worldRenderer.pos(x + z, y + offset, 0.0).color(red, green, blue, alpha).endVertex();
		worldRenderer.pos(x + z, y + 0.0, 0.0).color(red, green, blue, alpha).endVertex();
		Tessellator.getInstance().draw();
	}


	public static void drawRectangle(int left, int top, int right, int bottom, int color) {
		drawRect(left, top, right, bottom, color);
	}

	public static String trimStringToWidth(String text, int width) {
		return text == null ? null : mc().fontRendererObj.trimStringToWidth(text, width, false);
	}

	public static void drawRect(double left, double top, double right, double bottom, int color) {
		double j;
		if (left < right) {
			j = left;
			left = right;
			right = j;
		}

		if (top < bottom) {
			j = top;
			top = bottom;
			bottom = j;
		}

		float f3 = (float)(color >> 24 & 255) / 255.0F;
		float f = (float)(color >> 16 & 255) / 255.0F;
		float f1 = (float)(color >> 8 & 255) / 255.0F;
		float f2 = (float)(color & 255) / 255.0F;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(f, f1, f2, f3);
		worldrenderer.begin(7, DefaultVertexFormats.POSITION);
		worldrenderer.pos(left, bottom, 0.0).endVertex();
		worldrenderer.pos(right, bottom, 0.0).endVertex();
		worldrenderer.pos(right, top, 0.0).endVertex();
		worldrenderer.pos(left, top, 0.0).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	public static void drawCenteredString(String text, double x, double y) {
		drawString(text, x - (double)(getStringWidth(text) / 2), y);
	}

	public static void drawCenteredString(String text, double x, double y, double size) {
		GL11.glPushMatrix();
		GL11.glScaled(size, size, size);
		drawCenteredString(text, x / size, y / size);
		GL11.glPopMatrix();
	}

	public static int getStringWidth(String text) {
		return mc().fontRendererObj.getStringWidth(text);
	}

	public static void drawString(String text, double x, double y) {
		mc().fontRendererObj.drawString(text, (float)x, (float)y, 16777215, true);
	}

	public static void drawString(String text, double x, double y, double size) {
		GL11.glPushMatrix();
		GL11.glScaled(size, size, size);
		drawString(text, x / size, y / size);
		GL11.glPopMatrix();
	}

	public static void bindTexture(ResourceLocation resourceLocation) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
	}

	public static void bindTexture(String resourceLocation) {
		bindTexture(new ResourceLocation(resourceLocation));
	}

	public static void drawTexture(double x, double y, double texturePosX, double texturePosY, double imageWidth, double imageHeight, double maxWidth, double maxHeight) {
		drawTexture(x, y, texturePosX, texturePosY, imageWidth, imageHeight, maxWidth, maxHeight, 1.0F);
	}
	public static void drawTexture(double x, double y, double texturePosX, double texturePosY, double imageWidth, double imageHeight, double maxWidth, double maxHeight, float alpha) {
		GL11.glPushMatrix();
		double sizeWidth = maxWidth / imageWidth;
		double sizeHeight = maxHeight / imageHeight;
		GL11.glScaled(sizeWidth, sizeHeight, 0.0);
		if (alpha <= 1.0F) {
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
		}

		drawUVTexture(x / sizeWidth, y / sizeHeight, texturePosX, texturePosY, x / sizeWidth + imageWidth - x / sizeWidth, y / sizeHeight + imageHeight - y / sizeHeight);
		if (alpha <= 1.0F) {
			GlStateManager.disableAlpha();
			GlStateManager.disableBlend();
		}

		GL11.glPopMatrix();
	}

	private static void drawUVTexture(double x, double y, double textureX, double textureY, double width, double height) {
		float f = 0.00390625F;
		float f1 = 0.00390625F;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(x + 0.0, y + height, zLevel).tex((float)(textureX + 0.0) * f, (float)(textureY + height) * f1).endVertex();
		worldrenderer.pos(x + width, y + height, zLevel).tex((float)(textureX + width) * f, (float)(textureY + height) * f1).endVertex();
		worldrenderer.pos(x + width, y + 0.0, zLevel).tex((float)(textureX + width) * f, (float)(textureY + 0.0) * f1).endVertex();
		worldrenderer.pos(x + 0.0, y + 0.0, zLevel).tex((float)(textureX + 0.0) * f, (float)(textureY + 0.0) * f1).endVertex();
		tessellator.draw();
	}


	public static void drawTexture(double x, double y, double imageWidth, double imageHeight, double maxWidth, double maxHeight) {
		drawTexture(x, y, imageWidth, imageHeight, maxWidth, maxHeight, 1.0F);
	}

	public static void drawTexture(double x, double y, double imageWidth, double imageHeight, double maxWidth, double maxHeight, float alpha) {
		GL11.glPushMatrix();
		double sizeWidth = maxWidth / imageWidth;
		double sizeHeight = maxHeight / imageHeight;
		GL11.glScaled(sizeWidth, sizeHeight, 0.0);
		if (alpha <= 1.0F) {
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
		}

		drawTexturedModalRect(x / sizeWidth, y / sizeHeight, x / sizeWidth + imageWidth, y / sizeHeight + imageHeight);
		if (alpha <= 1.0F) {
			GlStateManager.disableAlpha();
			GlStateManager.disableBlend();
		}

		GL11.glPopMatrix();
	}

	public static void drawAutoDimmedBackground(double d) {
		if (player() != null && world() != null) {
			drawIngameBackground();
		} else {
			drawDimmedBackground((int)d);
		}

	}

	public static void drawDimmedBackground(int scroll) {
		drawBackground(0, -scroll, 32);
	}

	public static void drawBackground(int tint, double scrolling, int brightness) {
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		mc().getTextureManager().bindTexture(Gui.optionsBackground);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		worldrenderer.pos(0.0, getHeight(), 0.0).tex(0.0, ((double)getHeight() + scrolling) / 32.0 + (double)tint).color(brightness, brightness, brightness, 255).endVertex();
		worldrenderer.pos(getWidth(), getHeight(), 0.0).tex((float)getWidth() / 32.0F, ((double)getHeight() + scrolling) / 32.0 + (double)tint).color(brightness, brightness, brightness, 255).endVertex();
		worldrenderer.pos(getWidth(), 0.0, 0.0).tex((float)getWidth() / 32.0F, (double)tint + scrolling / 32.0).color(brightness, brightness, brightness, 255).endVertex();
		worldrenderer.pos(0.0, 0.0, 0.0).tex(0.0, (double)tint + scrolling / 32.0).color(brightness, brightness, brightness, 255).endVertex();
		tessellator.draw();
	}

	public static void drawIngameBackground() {
		int right = getWidth();
		int bottom = getHeight();
		float f = (float)(-1072689136 >> 24 & 255) / 255.0F;
		float f1 = (float)(-1072689136 >> 16 & 255) / 255.0F;
		float f2 = (float)(-1072689136 >> 8 & 255) / 255.0F;
		float f3 = (float)(-1072689136 & 255) / 255.0F;
		float f4 = (float)(-804253680 >> 24 & 255) / 255.0F;
		float f5 = (float)(-804253680 >> 16 & 255) / 255.0F;
		float f6 = (float)(-804253680 >> 8 & 255) / 255.0F;
		float f7 = (float)(-804253680 & 255) / 255.0F;
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(7425);
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		worldrenderer.pos(right, 0, zLevel).color(f1, f2, f3, f).endVertex();
		worldrenderer.pos(0, 0, zLevel).color(f1, f2, f3, f).endVertex();
		worldrenderer.pos(0, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
		worldrenderer.pos(right, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
		tessellator.draw();
		GlStateManager.shadeModel(7424);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	public static void drawTexturedModalRect(double left, double top, double right, double bottom) {
		double textureX = 0.0;
		double textureY = 0.0;
		double width = right - left;
		double height = bottom - top;
		float f = 0.00390625F;
		float f1 = 0.00390625F;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(left + 0.0, top + height, zLevel).tex((float)(textureX + 0.0) * f, (float)(textureY + height) * f1).endVertex();
		worldrenderer.pos(left + width, top + height, zLevel).tex((float)(textureX + width) * f, (float)(textureY + height) * f1).endVertex();
		worldrenderer.pos(left + width, top + 0.0, zLevel).tex((float)(textureX + width) * f, (float)(textureY + 0.0) * f1).endVertex();
		worldrenderer.pos(left + 0.0, top + 0.0, zLevel).tex((float)(textureX + 0.0) * f, (float)(textureY + 0.0) * f1).endVertex();
		tessellator.draw();
	}

	private static final ModelSkeletonHead humanoidHead = new ModelHumanoidHead();
	public static void renderSkull(GameProfile gameProfile) {
		Pair<String, String> skin = LabyBridge.labyBridge.getCachedTexture(gameProfile.getId());
		if (skin != null) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(skin.a, skin.b));
			GlStateManager.pushMatrix();
			GlStateManager.disableCull();
			GlStateManager.enableRescaleNormal();
			GlStateManager.enableAlpha();
			GlStateManager.scale(-1.0F, 1.0F, 1.0F);
			GlStateManager.translate(0.0F, 0.2F, 0.0F);
			humanoidHead.render(null, 0.0F, 0.0F, 0.0F, 180.0F, 0.0F, 0.0625F);
			GlStateManager.popMatrix();
		}

	}
	public static void drawStringWithShadow(String text, double x, double y, int color) {
		Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text, (float)x, (float)y, color);
	}

	public static int toRGB(int r, int g, int b) {
		return toRGB(r, g, b, 0xFF);
	}

	public static int toRGB(int r, int g, int b, int a) {
		return (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
	}

	public static int getWidth() {
		return MinecraftUtil.screenWidth();
	}

	public static int getHeight() {
		return MinecraftUtil.screenHeight();
	}
}
