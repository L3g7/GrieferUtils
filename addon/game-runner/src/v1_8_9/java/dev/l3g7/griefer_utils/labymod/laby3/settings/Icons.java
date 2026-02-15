/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.settings;

import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.util.render.GlEngine;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.ControlElement.IconData;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public abstract class Icons {

	public static L3Icon of(String icon) {
		return new TextureIcon(new ResourceLocation("griefer_utils", "icons/" + icon + ".png"));
	}

	public static L3Icon of(ItemStack icon) {
		return new ItemStackIcon(icon);
	}

	public static abstract class L3Icon {
		public abstract void draw(int x, int y, float scale);

		public IconData toIconData() {
			return new WrappedIcon(this);
		}
	}

	public static class TextureIcon extends L3Icon {

		private final ResourceLocation location;

		public TextureIcon(ResourceLocation location) {
			this.location = location;
		}

		@Override
		public void draw(int x, int y, float scale) {
			GlStateManager.enableBlend();
			GlStateManager.color(1, 1, 1);
			mc().getTextureManager().bindTexture(location);
			DrawUtils.drawTexture(x + 3, y + 3, 0, 0, 256, 256, 16 * scale, 16 * scale, 2);
		}

	}

	public static class ItemStackIcon extends L3Icon {

		private final ItemStack stack;

		public ItemStackIcon(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public void draw(int x, int y, float scale) {
			GlEngine.begin();
			GlEngine.scale(scale);
			DrawUtils.drawItem(stack, x + 3 / scale, y + 2 / scale, null);
			GlEngine.finish();
		}

	}

	private static class WrappedIcon extends IconData {

		private final L3Icon icon;

		public WrappedIcon(L3Icon icon) {
			super((ResourceLocation) null);
			this.icon = icon;
		}

		@Override
		public boolean hasTextureIcon() {
			// ControlElement draws are intercepted -> hasTextureIcon only gets called for LabyModModuleEditorGui#masterIconData draws
			ScaledResolution scaled = LabyMod.getInstance().getDrawUtils().getScaledResolution();
			double scale = (double) scaled.getScaleFactor() / LabyMod.getInstance().getDrawUtils().getCustomScaling();

			int x = LabyMod.getSettings().moduleEditorSplitX - 25;
			int y = (int) (20d * scale) + 14;
			icon.draw(x, y, 1);
			return false;
		}

	}

	/**
	 * Injects a redirect to the Icon#draw function.
	 */
	@Mixin(ControlElement.class)
	public static class MixinControlElement {

		@Unique
		private int grieferUtils$x, grieferUtils$y;

		@Inject(method = "draw", at = @At("HEAD"), remap = false)
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY, CallbackInfo ci) {
			grieferUtils$x = x;
			grieferUtils$y = y;
		}

		@Redirect(method = "draw", at = @At(value = "INVOKE", target = "Lnet/labymod/settings/elements/ControlElement$IconData;hasTextureIcon()Z"), remap = false)
		public boolean draw(IconData data) {
			if (!(data instanceof WrappedIcon wrappedIcon))
				return data.hasTextureIcon();

			wrappedIcon.icon.draw(grieferUtils$x, grieferUtils$y, 1);
			return false;
		}

	}

}
