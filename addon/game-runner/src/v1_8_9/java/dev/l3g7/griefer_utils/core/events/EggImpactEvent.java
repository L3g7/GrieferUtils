/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events;

import dev.l3g7.griefer_utils.core.api.event.event_bus.Event;
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
