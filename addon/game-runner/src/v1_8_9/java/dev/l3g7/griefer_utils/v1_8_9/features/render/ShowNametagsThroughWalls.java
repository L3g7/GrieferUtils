/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.render;

import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLiving;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Singleton
public class ShowNametagsThroughWalls extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Nametags durch Wände anzeigen")
		.description("Zeigt Nametags auch durch Wände an.\n(Funktioniert nicht bei Spielern)")
		.icon("yellow_name");

	@Mixin(RenderLiving.class)
	private static abstract class MixinRenderLiving extends RendererLivingEntity<EntityLiving> {

		public MixinRenderLiving(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
			super(renderManagerIn, modelBaseIn, shadowSizeIn);
		}

		@Inject(method = "canRenderName(Lnet/minecraft/entity/EntityLiving;)Z", at = @At("RETURN"), cancellable = true)
	    private void injectCanRenderName(EntityLiving entity, CallbackInfoReturnable<Boolean> cir) {
	    	if (FileProvider.getSingleton(ShowNametagsThroughWalls.class).isEnabled() && !cir.getReturnValueZ())
				cir.setReturnValue(super.canRenderName(entity) && entity.hasCustomName());
	    }

	}

	@Mixin(value = Render.class, priority = 1001)
	private static class MixinRender {

		@Redirect(method = "renderLivingLabel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;tryBlendFuncSeparate(IIII)V"))
		private void redirectTryBlendFuncSeparate(int srcFactor, int dstFactor, int srcFactorAlpha, int dstFactorAlpha) {
			boolean enabled = FileProvider.getSingleton(ShowNametagsThroughWalls.class).isEnabled();
			GlStateManager.tryBlendFuncSeparate(enabled ? dstFactor : srcFactor, enabled ? srcFactor : dstFactor, srcFactorAlpha, dstFactorAlpha);
		}

	}

}
