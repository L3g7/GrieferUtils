/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.events.render;

import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;

/**
 * Used to lock the mouse
 */
public class ScaledResolutionInitEvent extends Event {

	public final ScaledResolution scaledResolution;

	public ScaledResolutionInitEvent(ScaledResolution scaledResolution) {
		this.scaledResolution = scaledResolution;
	}

	@Mixin(ScaledResolution.class)
	private static class MixinScaledResolution {

		@Inject(method = "<init>", at = @At("RETURN"))
		public void injectInit(Minecraft p_i46445_1_, CallbackInfo ci) {
			new ScaledResolutionInitEvent(c(this)).fire();
		}

	}

}
