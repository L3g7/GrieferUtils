/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.render;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;
import static net.minecraft.client.renderer.OpenGlHelper.GL_FRAMEBUFFER;
import static net.minecraft.client.renderer.OpenGlHelper.GL_RENDERBUFFER;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT;
import static org.lwjgl.opengl.EXTPackedDepthStencil.GL_DEPTH24_STENCIL8_EXT;
import static org.lwjgl.opengl.GL11.*;

@Singleton
public class SkullEnchantmentFix extends Feature {

	public static final ItemStack ICON = ItemUtil.createItem(Items.skull, 0, true);

	@SuppressWarnings("unchecked")
	public static final IBakedModel cubeModel = new IBakedModel() {
		private final List<BakedQuad>[] bakedQuads = new List[] { // Data was copied from the IBakedModel of a standard dirt block
			ImmutableList.of(new BakedQuad(new int[] {0, 0, 1065353216, -8421505, 1048576655, 1044383007, 33024, 0, 0, 0, -8421505, 1048576655, 1046477537, 33024, 1065353216, 0, 0, -8421505, 1049623921, 1046477537, 33024, 1065353216, 0, 1065353216, -8421505, 1049623921, 1044383007, 33024}, -1, EnumFacing.DOWN)),
			ImmutableList.of(new BakedQuad(new int[] {0, 1065353216, 0, -1, 1048576655, 1044383007, 32512, 0, 1065353216, 1065353216, -1, 1048576655, 1046477537, 32512, 1065353216, 1065353216, 1065353216, -1, 1049623921, 1046477537, 32512, 1065353216, 1065353216, 0, -1, 1049623921, 1044383007, 32512}, -1, EnumFacing.UP)),
			ImmutableList.of(new BakedQuad(new int[] {1065353216, 1065353216, 0, -3355444, 1048576655, 1044383007, 8454144, 1065353216, 0, 0, -3355444, 1048576655, 1046477537, 8454144, 0, 0, 0, -3355444, 1049623921, 1046477537, 8454144, 0, 1065353216, 0, -3355444, 1049623921, 1044383007, 8454144}, -1, EnumFacing.NORTH)),
			ImmutableList.of(new BakedQuad(new int[] {0, 1065353216, 1065353216, -3355444, 1048576655, 1044383007, 8323072, 0, 0, 1065353216, -3355444, 1048576655, 1046477537, 8323072, 1065353216, 0, 1065353216, -3355444, 1049623921, 1046477537, 8323072, 1065353216, 1065353216, 1065353216, -3355444, 1049623921, 1044383007, 8323072}, -1, EnumFacing.SOUTH)),
			ImmutableList.of(new BakedQuad(new int[] {0, 1065353216, 0, -6710887, 1048576655, 1044383007, 129, 0, 0, 0, -6710887, 1048576655, 1046477537, 129, 0, 0, 1065353216, -6710887, 1049623921, 1046477537, 129, 0, 1065353216, 1065353216, -6710887, 1049623921, 1044383007, 129}, -1, EnumFacing.WEST)),
			ImmutableList.of(new BakedQuad(new int[] {1065353216, 1065353216, 1065353216, -6710887, 1048576655, 1044383007, 127, 1065353216, 0, 1065353216, -6710887, 1048576655, 1046477537, 127, 1065353216, 0, 0, -6710887, 1049623921, 1046477537, 127, 1065353216, 1065353216, 0, -6710887, 1049623921, 1044383007, 127}, -1, EnumFacing.EAST))
		};

		public List<BakedQuad> getFaceQuads(EnumFacing facing) {
			return bakedQuads[facing.getIndex()];
		}

		public List<BakedQuad> getGeneralQuads() { return new ArrayList<>(); }
		public boolean isAmbientOcclusion() { return false; }
		public boolean isGui3d() { return false; }
		public boolean isBuiltInRenderer() { return false; }
		public TextureAtlasSprite getParticleTexture() { return null; }
		public ItemCameraTransforms getItemCameraTransforms() { return null; }
	};

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Kopf-Verzauberung fixen")
		.description("Behebt, dass Verzauberungen von Köpfen nicht angezeigt werden.")
		.icon(ICON);

	@Override
	public void init() {
		super.init();
		FramebufferWithStencil fb = (FramebufferWithStencil) mc().getFramebuffer();
		if (!fb.isStencilEnabled())
			fb.enableStencil();
	}

	public interface FramebufferWithStencil {
		boolean isStencilEnabled();
		void enableStencil();
	}

	@Mixin(RenderItem.class)
	private static class MixinRenderItem {

		@Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityItemStackRenderer;renderByItem(Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER))
		public void injectRenderItem(ItemStack stack, IBakedModel model, CallbackInfo ci) {
			if (stack.getItem() != Items.skull || !stack.hasEffect())
				return;

			if (!FileProvider.getSingleton(SkullEnchantmentFix.class).isEnabled() && stack != ICON)
				return;

			// Enable stencil
			glClear(GL_STENCIL_BUFFER_BIT);
			glEnable(GL_STENCIL_TEST);
			glStencilFunc(GL_ALWAYS, 1, 0);
			glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);

			// Render skull to stencil buffer
			TileEntityItemStackRenderer.instance.renderByItem(stack);

			// Render enchantment glint
			glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
			glStencilFunc(GL_EQUAL, 1, 1);
			GlStateManager.enableBlend();

			GlStateManager.translate(-0.035, -0.035, -0.035);
			GlStateManager.scale(1.07, 1.07, 1.07);
			Reflection.invoke(mc().getRenderItem(), "renderEffect", cubeModel);

			// Disable stencil
			glDisable(GL_STENCIL_TEST);
		}

		@Inject(method = "renderEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;disableLighting()V"))
		public void injectRenderEffect(IBakedModel iBakedModel, CallbackInfo ci) {
			if (iBakedModel == cubeModel)
				GlStateManager.depthFunc(GL11.GL_LEQUAL);
		}

	}

	@Mixin(Framebuffer.class)
	@Implements(@Interface(iface = FramebufferWithStencil.class, prefix = "griefer_utils$"))
	private static abstract class MixinFramebuffer implements FramebufferWithStencil {

		@Shadow
		public abstract void createBindFramebuffer(int width, int height);

		@Shadow public int framebufferWidth;
		@Shadow public int framebufferHeight;
		@Shadow public int depthBuffer;

		@Unique
		private boolean grieferUtils$stencilEnabled = false;

		@ModifyArg(method = "createFramebuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OpenGlHelper;glRenderbufferStorage(IIII)V"), index = 1)
		public int modifyInternalFormat(int internalFormat) {
			return grieferUtils$stencilEnabled ? GL_DEPTH24_STENCIL8_EXT : internalFormat;
		}

		@Redirect(method = "createFramebuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OpenGlHelper;glFramebufferRenderbuffer(IIII)V"))
		private void redirectGlRenderbufferStorage(int target, int attachment, int renderBufferTarget, int renderBuffer) {
			if (!grieferUtils$stencilEnabled) {
				OpenGlHelper.glFramebufferRenderbuffer(target, attachment, renderBufferTarget, renderBuffer);
				return;
			}

			OpenGlHelper.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT_EXT, GL_RENDERBUFFER, this.depthBuffer);
			OpenGlHelper.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT_EXT, GL_RENDERBUFFER, this.depthBuffer);
		}

		public boolean griefer_utils$isStencilEnabled() { // Shim for vanilla
			return grieferUtils$stencilEnabled;
		}

		public void griefer_utils$enableStencil() {
			grieferUtils$stencilEnabled = true;
			this.createBindFramebuffer(framebufferWidth, framebufferHeight);
		}

	}

}
