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

package dev.l3g7.griefer_utils.event.events;

import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class RenderGameOverlayEvent extends Event {

	@Mixin(GuiIngame.class)
	private static class MixinGuiIngame {

	    @Inject(method = "renderGameOverlay", at = @At("TAIL"))
	    public void injectRenderGameOverlay(float partialTicks, CallbackInfo ci) {
		    MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent());
	    }

	}

	@Mixin(GuiIngameForge.class)
	private static class MixinGuiIngameForge {

		@Inject(method = "renderGameOverlay", at = @At("TAIL"))
		public void injectRenderGameOverlay(float partialTicks, CallbackInfo ci) {
			MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent());
		}

	}

}
