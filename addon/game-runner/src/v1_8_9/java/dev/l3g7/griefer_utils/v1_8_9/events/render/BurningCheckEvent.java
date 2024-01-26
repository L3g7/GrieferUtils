/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.events.render;

import dev.l3g7.griefer_utils.api.event.event_bus.Event.TypedEvent;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;

/**
 * An event being posted when {@link Entity#isBurning()} is called on a burning entity.
 */
public class BurningCheckEvent extends TypedEvent<BurningCheckEvent> {

	public final Entity entity;
	public boolean burning = true;

	public BurningCheckEvent(Entity entity) {
		this.entity = entity;
	}

	@Mixin(Entity.class)
	private static class MixinEntity {

		@Inject(method = "isBurning", at = @At("RETURN"), cancellable = true)
		private void injectIsBurning(CallbackInfoReturnable<Boolean> cir) {
			if (cir.getReturnValueZ())
				cir.setReturnValue(new BurningCheckEvent(c(this)).fire().burning);
		}

	}

}
