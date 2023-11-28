/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.render;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
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
	private final BooleanSetting enabled = new BooleanSetting()
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

	@Mixin(Render.class)
	private static class MixinRender {

		@Redirect(method = "renderLivingLabel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;tryBlendFuncSeparate(IIII)V"))
		private void redirectTryBlendFuncSeparate(int srcFactor, int dstFactor, int srcFactorAlpha, int dstFactorAlpha) {
			boolean enabled = FileProvider.getSingleton(ShowNametagsThroughWalls.class).isEnabled();
			GlStateManager.tryBlendFuncSeparate(enabled ? dstFactor : srcFactor, enabled ? srcFactor : dstFactor, srcFactorAlpha, dstFactorAlpha);
		}

	}

}
