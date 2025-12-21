/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings;

import dev.l3g7.griefer_utils.core.api.misc.functions.Supplier;
import dev.l3g7.griefer_utils.core.util.render.AsyncSkullRenderer;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.gui.screen.state.ScreenCanvas;
import net.labymod.api.client.render.batch.ResourceRenderContext;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.util.bounds.Rectangle;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

public class Icons {
	public static final Icon OWN_SKULL = new SkullIcon();

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

		Icon labyIcon = of(icon);
		return new ProxiedIcon(() -> labyIcon, offsetX, offsetY);
	}

	/**
	 * An icon that uses the direct rendering API instead of submitting render calls.
	 */
	public abstract static class SynchronousIcon extends Icon {
		protected SynchronousIcon() {
			super(null);
		}

		@Override
		public void render(ResourceRenderContext context, float x, float y, float width, float height, boolean hover, int color) {
			render(null, x, y, width, height, hover, color, null, false);
		}

		@Override
		public void render(Stack stack, float x, float y, float width, float height, boolean hover, int color, Rectangle stencil) {
			render(stack, x, y, width, height, hover, color, stencil, false);
		}

		public abstract void render(Stack stack, float x, float y, float width, float height, boolean hover, int color, Rectangle stencil, boolean submitted);
	}

	public static class ItemStackIcon extends SynchronousIcon {

		private final ItemStack icon;
		private final int offsetX, offsetY;
		private final float scale;

		public ItemStackIcon(ItemStack icon, int offsetX, int offsetY, float scale) {
			this.icon = icon;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.scale = scale;
		}

		@Override
		public void render(Stack stack, float x, float y, float width, float height, boolean hover, int color, Rectangle stencil, boolean submitted) {
			// Fix position for scales < 16
			x += -1.5f * width + 24;
			y += -1.25f * height + 20;

			x += offsetX;
			y += offsetY;

			if (submitted)
				GlStateManager.enableDepth();

			GlStateManager.scale(width / 16f * scale, height / 16f * scale, 1);
			Laby.labyAPI().minecraft().itemStackRenderer().renderItemStack(stack, c(icon), (int) (x / scale), (int) (y / scale));
			GlStateManager.scale(16f / width / scale, 16f / height / scale, 1);
		}

	}

	public static class ProxiedIcon extends Icon {

		public final float offsetX, offsetY;
		private final Supplier<Icon> icon;

		public ProxiedIcon(Supplier<Icon> icon, float offsetX, float offsetY) {
			super(null);
			this.icon = icon;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
		}

		public Icon getIcon() {
			return icon.get();
		}
	}

	private static class SkullIcon extends SynchronousIcon {
		@Override
		public void render(Stack stack, float x, float y, float width, float height, boolean hover, int color, Rectangle stencil, boolean submitted) {
			AsyncSkullRenderer.renderPlayerSkull((int) x, (int) y);
		}
	}

	@Mixin(value = ScreenCanvas.class, remap = false)
	private static abstract class ScreenCanvasMixin {

		@Shadow
		public abstract void submitIcon(Icon icon, float x, float y, float width, float height, boolean hover, int argb, @Nullable Rectangle clipBounds);

		@Shadow
		@Final
		private ScreenContext context;

		@Inject(method = "submitIcon(Lnet/labymod/api/client/gui/icon/Icon;FFFFZILnet/labymod/api/util/bounds/Rectangle;)V", at = @At("HEAD"), cancellable = true, remap = false)
		public void submitIcon(Icon icon, float x, float y, float width, float height, boolean hover, int argb, Rectangle clipBounds, CallbackInfo ci) {
			switch (icon) {
				case ProxiedIcon proxiedIcon -> {
					submitIcon(proxiedIcon.getIcon(), x + proxiedIcon.offsetX, y + proxiedIcon.offsetY, width, height, hover, argb, clipBounds);
					ci.cancel();
				}
				case SynchronousIcon synchronousIcon -> {
					synchronousIcon.render(context.stack(), x, y, width, height, hover, argb, clipBounds, true);
					ci.cancel();
				}
				default -> {
					// Continue original submitIcon
				}
			}
		}
	}

}
