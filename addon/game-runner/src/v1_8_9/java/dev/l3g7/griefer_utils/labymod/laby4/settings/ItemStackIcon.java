/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings;

import net.labymod.api.Laby;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.render.batch.ResourceRenderContext;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.world.item.ItemStack;
import net.labymod.api.util.bounds.Rectangle;
import net.minecraft.client.renderer.GlStateManager;

public class ItemStackIcon extends Icon { // NOTE: move somewhere else

	private final ItemStack icon;
	private final int offsetX, offsetY;
	private final float scale;

	public ItemStackIcon(ItemStack icon) {
		this(icon, 0, 0, 1);
	}

	public ItemStackIcon(ItemStack icon, boolean asEntry) {
		this(icon, asEntry ? -2 : 0, asEntry ? -1 : 0, 1);
	}

	public ItemStackIcon(ItemStack icon, int offsetX, int offsetY, float scale) {
		super(null);
		this.icon = icon;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.scale = scale;
	}

	@Override
	public void render(ResourceRenderContext context, float x, float y, float width, float height, boolean hover, int color) {
		render(null, x, y, width, height, hover, color, null);
	}

	@Override
	public void render(Stack stack, float x, float y, float width, float height, boolean hover, int color, Rectangle stencil) {
		// Fix position for scales < 16
		x += -1.5f * width + 24;
		y += -1.25f * height + 20;

		x += offsetX;
		y += offsetY;

		GlStateManager.scale(width / 16f * scale, height / 16f * scale, 1);
		Laby.labyAPI().minecraft().itemStackRenderer().renderItemStack(stack, icon, (int) (x / scale), (int) (y / scale));
		GlStateManager.scale(16f / width / scale, 16f / height / scale, 1);
	}

}
