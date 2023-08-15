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

import dev.l3g7.griefer_utils.core.event_bus.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.spongepowered.asm.mixin.injection.At.Shift.AFTER;
import static org.spongepowered.asm.mixin.injection.At.Shift.BEFORE;

public abstract class TickEvent extends Event {

	public static class RenderTickEvent extends TickEvent {

		public final float renderTickTime;

		public RenderTickEvent(float renderTickTime) {
			this.renderTickTime = renderTickTime;
		}

		@Mixin(Minecraft.class)
		private static class MixinMinecraft {

			@Shadow
			private Timer timer;

			@Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", ordinal = 3, shift = AFTER))
			public void injectRunGameLoopPost(CallbackInfo ci) {
				new RenderTickEvent(timer.renderPartialTicks).fire();
			}

		}

	}

	public static class ClientTickEvent extends TickEvent {

		@Mixin(Minecraft.class)
		private static class MixinMinecraft {

			@Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", ordinal = 1, shift = BEFORE))
			public void injectRunTick(CallbackInfo ci) {
				new ClientTickEvent().fire();
			}

		}

	}

}
