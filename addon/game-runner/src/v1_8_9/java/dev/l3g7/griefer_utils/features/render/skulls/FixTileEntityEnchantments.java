/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.render.skulls;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static net.minecraft.client.renderer.OpenGlHelper.GL_FRAMEBUFFER;
import static net.minecraft.client.renderer.OpenGlHelper.GL_RENDERBUFFER;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT;
import static org.lwjgl.opengl.EXTPackedDepthStencil.GL_DEPTH24_STENCIL8_EXT;
import static org.lwjgl.opengl.GL11.*;
import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;

/**
 * Adds enchantment glint to tile entities by rendering the enchantment of a dirt block onto the tile entity
 * via stencil buffer.
 */
@Singleton
public class FixTileEntityEnchantments extends Feature {

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
		.name(LABY_3.isActive()
			? "Tile Entity- Verzauberung fixen" // Spacing to allow word wrap
			: "Tile Entity-Verzauberung fixen")
		.description("Behebt, dass Verzauberungen von Tile Entities (Bannern, Kisten, Köpfen) nicht angezeigt werden.")
		.icon("enchanted_steve");

	public static FixTileEntityEnchantments get() {
		return get(FixTileEntityEnchantments.class);
	}

	@Mixin(RenderItem.class)
	private static class MixinRenderItem {

		@Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityItemStackRenderer;renderByItem(Lnet/minecraft/item/ItemStack;)V", shift = AFTER))
		public void injectRenderItem(ItemStack stack, IBakedModel model, CallbackInfo ci) {
			if ((stack.getItem() != Items.skull
				&& stack.getItem() != Items.banner
				&& stack.getItem() != Item.getItemFromBlock(Blocks.chest)
				&& stack.getItem() != Item.getItemFromBlock(Blocks.ender_chest)
				&& stack.getItem() != Item.getItemFromBlock(Blocks.trapped_chest)
			) || !stack.hasEffect())
				return;

			if (!FixTileEntityEnchantments.get().isEnabled())
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
			if (stack.getItem() == Items.banner)
				GlStateManager.scale(1.07, 1.85, 1.07);
			else
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

	/**
	 * Adds a stencil buffer to Minecraft's Framebuffer.
	 */
	@Mixin(Framebuffer.class)
	private static class MixinFramebuffer {

		@Shadow
		public int depthBuffer;

		@ModifyArg(method = "createFramebuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OpenGlHelper;glRenderbufferStorage(IIII)V"), index = 1)
		public int modifyInternalFormat(int internalFormat) {
			return GL_DEPTH24_STENCIL8_EXT;
		}

		@Inject(method = "createFramebuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OpenGlHelper;glFramebufferRenderbuffer(IIII)V", shift = AFTER))
		private void injectCreateFramebuffer(int displayWidth, int displayHeight, CallbackInfo ci) {
			OpenGlHelper.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT_EXT, GL_RENDERBUFFER, this.depthBuffer);
		}

	}

	/**
	 * Adds a stencil buffer to OptiFine's Framebuffer.
	 */
	@Pseudo
	@SuppressWarnings("UnresolvedMixinReference")
	@Mixin(targets = "net.optifine.shaders.Shaders", remap = false)
	private static abstract class MixinShaders {

		@Redirect(method = "setupFrameBuffer", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTexImage2D(IIIIIIIILjava/nio/FloatBuffer;)V"))
		private static void injectTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, FloatBuffer pixels) {
			GL11.glTexImage2D(target, level, GL30.GL_DEPTH32F_STENCIL8, width, height, border, GL30.GL_DEPTH_STENCIL, GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV, pixels);
		}

		@Redirect(method = "setupFrameBuffer", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/EXTFramebufferObject;glFramebufferTexture2DEXT(IIIII)V", ordinal = 0))
		private static void injectBindFramebuffer(int target, int attachment, int texTarget, int texture, int level) {
			EXTFramebufferObject.glFramebufferTexture2DEXT(target, GL30.GL_DEPTH_STENCIL_ATTACHMENT, texTarget, texture, level);
		}
	}

}
