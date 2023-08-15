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
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.util.MovingObjectPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class EggImpactEvent extends Event {

	public final MovingObjectPosition mop;

	public EggImpactEvent(MovingObjectPosition mop) {
		this.mop = mop;
	}

	@Mixin(EntityEgg.class)
	private static class MixinEntityEgg {

		@Inject(method = "onImpact", at = @At("HEAD"), cancellable = true)
		public void injectOnImpact(MovingObjectPosition mop, CallbackInfo ci) {
			if (new EggImpactEvent(mop).fire().isCanceled())
				ci.cancel();
		}

	}


}
