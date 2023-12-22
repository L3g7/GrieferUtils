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

package dev.l3g7.griefer_utils.event.events.render;

import dev.l3g7.griefer_utils.core.event_bus.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class RenderWorldLastEvent extends Event {

	public final float partialTicks;

	public RenderWorldLastEvent(float partialTicks) {
		this.partialTicks = partialTicks;
	}

	@Mixin(EntityRenderer.class)
	private static class MixinEntityRenderer {

		@Shadow
		private Minecraft mc;

		@Inject(method = "renderWorldPass", at = @At(value = "INVOKE_STRING", target= "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", args = "ldc=hand", shift = At.Shift.BEFORE))
	    public void injectRenderWorldPass(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
		    mc.mcProfiler.endStartSection("gu_render_last");
			new RenderWorldLastEvent(partialTicks).fire();
	    }

	}

}
