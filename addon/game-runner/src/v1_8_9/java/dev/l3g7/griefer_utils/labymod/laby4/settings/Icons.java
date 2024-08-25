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
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.util.bounds.Rectangle;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

public class Icons {

	public static Icon of(Object icon) {
		return switch (icon) {
			case null -> null;
			case String fileName -> of(ResourceLocation.create("griefer_utils", "icons/" + fileName + ".png"));
			case ResourceLocation location -> Icon.texture(location);
			case Icon i -> i;
			case Item item -> of(new ItemStack(item));
			case Block block -> of(new ItemStack(block));
			case ItemStack stack -> new ItemStackIcon(stack, 0, 0, 1);
			default ->
				throw new UnsupportedOperationException(icon.getClass().getSimpleName() + " is an unsupported icon type!");
		};

	}

	public static Icon of(Object icon, float offsetX, float offsetY) {
		return of(icon, offsetX, offsetY, 1);
	}

	public static Icon of(Object icon, float offsetX, float offsetY, float scale) {
		if (icon instanceof ItemStack stack)
			return new ItemStackIcon(stack, (int) offsetX, (int) offsetY, scale);

		if (scale != 1)
			throw new UnsupportedOperationException(icon.getClass().getSimpleName() + " does not support scaling!");

		return new OffsetIcon(of(icon), offsetX, offsetY);
	}

	private static class ItemStackIcon extends Icon {

		private final ItemStack icon;
		private final int offsetX, offsetY;
		private final float scale;

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
			Laby.labyAPI().minecraft().itemStackRenderer().renderItemStack(stack, c(icon), (int) (x / scale), (int) (y / scale));
			GlStateManager.scale(16f / width / scale, 16f / height / scale, 1);
		}

	}

	private static class OffsetIcon extends Icon {

		private final float offsetX, offsetY;
		private final Icon icon;

		public OffsetIcon(Icon icon, float offsetX, float offsetY) {
			super(null);
			this.icon = icon;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
		}

		@Override
		public void render(Stack stack, float x, float y, float width, float height, boolean hover, int color, Rectangle stencil) {
			icon.render(stack, x + offsetX, y + offsetY, width, height, hover, color, stencil);
		}

		@Override
		public void render(ResourceRenderContext context, float x, float y, float width, float height, boolean hover, int color) {
			icon.render(context, x + offsetX, y + offsetY, width, height, hover, color);
		}

	}
}
