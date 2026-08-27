/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Supplier;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.core.util.render.AsyncSkullRenderer;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.gui.screen.state.ScreenCanvas;
import net.labymod.api.client.gui.screen.state.states.AbstractGuiRenderState;
import net.labymod.api.client.gui.screen.util.scissor.ScissorArea;
import net.labymod.api.client.render.font.RenderableComponent;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.laby3d.pipeline.RenderStates;
import net.labymod.api.laby3d.pipeline.material.GuiMaterial;
import net.labymod.api.util.bounds.Rectangle;
import net.labymod.core.client.render.font.component.DefaultComponentRendererBuilder;
import net.labymod.laby3d.api.vertex.VertexConsumer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static org.lwjgl.opengl.GL11.*;

public class Icons {
	public static final Icon OWN_SKULL = new SkullIcon();

	public static Icon of(String icon) {
		return Icon.texture(ResourceLocation.create("griefer_utils", "icons/" + icon + ".png"));
	}

	public static Icon of(ItemStack icon) {
		return of(icon, 1);
	}

	public static Icon of(ItemStack icon, float scale) {
		return new ItemStackIcon(icon, scale);
	}

	public static Icon offset(Icon icon, float offsetX, float offsetY) {
		return new ProxiedIcon(() -> icon, offsetX, offsetY);
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

	/**
	 * An icon that uses the direct rendering API instead of submitting render calls.
	 */
	public abstract static class SynchronousIcon extends Icon {

		protected SynchronousIcon() {
			super(null);
		}

		public abstract void renderSynchronous(ScreenContext context, float x, float y, float width, float height);

		public void buildVertices(ScreenContext context, Matrix4f pose, float x, float y, float width, float height, @Nullable ScissorArea scissorArea) {
			// Enable scissor
			if (scissorArea != null) {
				if (scissorArea.getPose() != null)
					throw new UnsupportedOperationException("Scissor with pose");

				int guiScale = MinecraftUtil.screenScaling();
				int screenHeight = MinecraftUtil.mc().displayHeight;

				Rectangle bounds = scissorArea.bounds();

				// GUI space -> framebuffer pixels (origin bottom-left)
				int left = (int) (bounds.getLeft() * guiScale);
				int bottom = (int) (bounds.getBottom() * guiScale);
				int w = (int) (bounds.getWidth() * guiScale);
				int h = (int) (bounds.getHeight() * guiScale);

				glEnable(GL_SCISSOR_TEST);
				glScissor(left, screenHeight - bottom, w, h);
			}

			GlStateManager.pushMatrix();

			try (MemoryStack stack = MemoryStack.stackPush()) {
				long buffer = stack.nmalloc(8, 16 * 8);

				// Merge current and pose matrix
				nglGetDoublev(GL_MODELVIEW_MATRIX, buffer);
				Matrix4f merged = new Matrix4f(new Matrix4d().setFromAddress(buffer));
				pose.mul(merged, merged);

				// Load merged matrix into OpenGL
				merged.getToAddress(buffer);
				nglLoadMatrixf(buffer);
			}

			// Actual render
			this.renderSynchronous(context, x, y, width, height);

			GlStateManager.popMatrix();

			// Disable scissor
			if (scissorArea != null) {
				glDisable(GL_SCISSOR_TEST);
			}
		}

	}

	public static class ItemStackIcon extends SynchronousIcon {
		private final ItemStack icon;
		private final float scale;

		public ItemStackIcon(ItemStack icon, float scale) {
			this.icon = icon;
			this.scale = scale;
		}

		@Override
		public void renderSynchronous(ScreenContext context, float x, float y, float width, float height) {
			GlStateManager.scale(width / 16f * scale, height / 16f * scale, 1);
			Laby.references().itemStackVisualizer().submitItem(context, c(icon), (int) (x / scale), (int) (y / scale));
			GlStateManager.scale(16f / width / scale, 16f / height / scale, 1);
		}
	}

	private static class SkullIcon extends SynchronousIcon {
		@Override
		public void renderSynchronous(ScreenContext context, float x, float y, float width, float height) {
			AsyncSkullRenderer.renderPlayerSkull((int) x, (int) y);
		}
	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = ScreenCanvas.class, remap = false)
	private static abstract class ScreenCanvasMixin {

		@Shadow
		public abstract void submitIcon(Icon icon, float x, float y, float width, float height, boolean hover, int argb, @Nullable Rectangle clipBounds);

		@Shadow
		public abstract Matrix4f currentPose();

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
					ScreenCanvas self = c(this);
					self.submitState(new SynchronousRenderState(context, synchronousIcon, currentPose(), x, y, width, height, self.getScissorArea()));
					ci.cancel();
				}
				default -> {
					// Continue original submitIcon
				}
			}
		}
	}

	@ExclusiveTo(LABY_4)
	@Mixin(value = DefaultComponentRendererBuilder.class, remap = false)
	private static abstract class DefaultComponentRendererBuilderMixin {

		@Inject(method = "renderIcon(Lnet/labymod/api/client/gui/screen/ScreenContext;Lnet/labymod/api/client/render/font/RenderableComponent;FFLjava/util/function/Function;Z)V", at = @At(value = "INVOKE", target = "Lnet/labymod/api/client/gui/screen/state/ScreenCanvas;submitIcon(Lnet/labymod/api/client/gui/icon/Icon;FFFFZI)V"), cancellable = true, remap = false)
		public void submitIcon(ScreenContext context, RenderableComponent text, float x, float y, Function<RenderableComponent, Integer> baseColor, boolean allowColors, CallbackInfo ci) {
			if (text.getIcon().getIcon() instanceof SynchronousIcon)
				ci.cancel();
		}
	}

	public static class SynchronousRenderState extends AbstractGuiRenderState {
		private final ScreenContext context;
		private final SynchronousIcon icon;

		public SynchronousRenderState(ScreenContext context, SynchronousIcon icon, Matrix4f pose, float x, float y, float width, float height, @Nullable ScissorArea scissorArea) {
			super(GuiMaterial.untextured(RenderStates.GUI_TEXTURED), pose, x, y, width, height, scissorArea);
			this.context = context;
			this.icon = icon;
		}

		@Override
		public void buildVertices(VertexConsumer consumer) {
			icon.buildVertices(context, pose(), left, top, right - left, bottom - top, getScissorArea());
		}
	}
}
