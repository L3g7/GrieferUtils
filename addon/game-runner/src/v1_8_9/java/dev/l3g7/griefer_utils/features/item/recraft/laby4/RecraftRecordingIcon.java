/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft.laby4;

import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftRecordingCore;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.render.batch.ResourceRenderContext;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.util.bounds.Rectangle;
import net.labymod.v1_8_9.client.util.MinecraftUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.features.item.recraft.RecraftRecording.ROMAN_NUMERALS;

class RecraftRecordingIcon extends Icon { // NOTE: move somewhere else

	private final RecraftRecording recording;

	RecraftRecordingIcon(RecraftRecording recording) {
		super(null);
		this.recording = recording;
	}

	@Override
	public void render(ResourceRenderContext context, float x, float y, float width, float height, boolean hover, int color) {
		render(null, x, y, width, height, hover, color, null);
	}

	@Override
	public void render(Stack stack, float x, float y, float width, float height, boolean hover, int color, Rectangle stencil) {
		if (recording.icon == null)
			return;

		if (recording.icon.getItem() == Item.getItemFromBlock(Blocks.barrier))
			recording.icon.stackSize = 0;

		float alpha = Laby.labyAPI().renderPipeline().getAlpha();
		if (alpha == 0)
			return;

		// Fix position for scales < 16
		x += -1.5f * width + 24;
		y += -1.25f * height + 20;

		GlStateManager.scale(width / 16f, height / 16f, 1);

		Laby.labyAPI().minecraft().itemStackRenderer().renderItemStack(stack, MinecraftUtil.fromMinecraft(recording.icon), (int) x, (int) y, false, alpha);
		mc().getRenderItem().renderItemOverlayIntoGUI(mc().fontRendererObj, recording.icon, (int) x, (int) y, ROMAN_NUMERALS[recording.icon.stackSize]);
		RenderHelper.disableStandardItemLighting();

		GlStateManager.pushMatrix();
		GlStateManager.scale(0.5, 0.5, 1);
		GlStateManager.translate(0, 0, 250);
		double sX = (x + 10) * 2;
		double sY = (y + 1) * 2;

		if (recording.mode().get() == RecraftRecordingCore.RecordingMode.CRAFT) {
			DrawUtils.drawItem(ItemUtil.createItem(new ItemStack(Blocks.crafting_table), true, null), sX, sY, null);
		} else {
			String icon = recording.mode().get() == RecraftRecordingCore.RecordingMode.RECIPE ? "knowledge_book" : "chest";
			DrawUtils.bindTexture(new ResourceLocation("griefer_utils", "/icons/" + icon + ".png"));
			DrawUtils.drawTexture(sX, sY, 256, 256, 16, 16);
		}
		GlStateManager.popMatrix();

		GlStateManager.scale(16f / width, 16f / height, 1);
	}

}