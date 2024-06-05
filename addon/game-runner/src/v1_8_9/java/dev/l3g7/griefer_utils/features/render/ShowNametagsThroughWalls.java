/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.render;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;

@Singleton
@ExclusiveTo(LABY_3)
public class ShowNametagsThroughWalls extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Nametags durch Wände anzeigen")
		.description("Zeigt Nametags auch durch Wände an.\n(Funktioniert nicht bei Spielern)")
		.icon("yellow_name");

	// NOTE Implement https://github.com/L3g7/GrieferUtils/commit/421d52f50619ddf05f942548d70b0c9648614b6c when merging with LabyMod 3

	@Mixin(RenderLiving.class)
	@ExclusiveTo(LABY_3)
	private static abstract class MixinRenderLiving extends RendererLivingEntity<EntityLiving> {

		public MixinRenderLiving(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
			super(renderManagerIn, modelBaseIn, shadowSizeIn);
		}

		@Inject(method = "canRenderName(Lnet/minecraft/entity/EntityLiving;)Z", at = @At("RETURN"), cancellable = true)
	    private void injectCanRenderName(EntityLiving entity, CallbackInfoReturnable<Boolean> cir) {
	    	if (FileProvider.getSingleton(ShowNametagsThroughWalls.class).isEnabled() && !cir.getReturnValueZ()) {
			    cir.setReturnValue(super.canRenderName(entity) && entity.hasCustomName());
		    }

	    }

	}

	@Mixin(value = Render.class, priority = 1001)
	@ExclusiveTo(LABY_3)
	private static class MixinRender {

		private static boolean renderingLivingEntity;

		@Inject(method = "renderLivingLabel", at = @At("HEAD"))
		private void injectRenderLivingLabel(Entity entityIn, String str, double x, double y, double z, int maxDistance, CallbackInfo ci) {
			renderingLivingEntity = entityIn instanceof EntityLiving;
		}

		@Redirect(method = "renderLivingLabel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;tryBlendFuncSeparate(IIII)V"))
		private void redirectTryBlendFuncSeparate(int srcFactor, int dstFactor, int srcFactorAlpha, int dstFactorAlpha) {
			boolean swap = renderingLivingEntity && FileProvider.getSingleton(ShowNametagsThroughWalls.class).isEnabled();
			GlStateManager.tryBlendFuncSeparate(swap ? dstFactor : srcFactor, swap ? srcFactor : dstFactor, srcFactorAlpha, dstFactorAlpha);
		}

	}

}
