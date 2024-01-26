/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.v1_8_9.events;

import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class MouseClickEvent extends Event {

	public static class LeftClickEvent extends MouseClickEvent {}

	public static class RightClickEvent extends MouseClickEvent {}

	@Mixin(Minecraft.class)
	private static class MixinMinecraft {

		@Inject(method = "clickMouse", at = @At("HEAD"), cancellable = true)
		public void injectClickMouse(CallbackInfo ci) {
			if (new MouseClickEvent.LeftClickEvent().fire().isCanceled())
				ci.cancel();
		}

		@Inject(method = "rightClickMouse", at = @At("HEAD"), cancellable = true)
		public void injectRightClickMouse(CallbackInfo ci) {
			if (new MouseClickEvent.RightClickEvent().fire().isCanceled())
				ci.cancel();
		}

		@ModifyArg(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;sendClickBlockToController(Z)V"))
		public boolean injectSendClickBlockToController(boolean leftClick) {
			return leftClick && !new MouseClickEvent.LeftClickEvent().fire().isCanceled();
		}

	}

}
