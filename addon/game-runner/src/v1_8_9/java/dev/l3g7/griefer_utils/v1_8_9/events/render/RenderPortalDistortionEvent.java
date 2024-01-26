/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.events.render;

import dev.l3g7.griefer_utils.api.event.event_bus.Event.TypedEvent;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

public class RenderPortalDistortionEvent extends TypedEvent<RenderPortalDistortionEvent> {

	public float distortion;

	public RenderPortalDistortionEvent(float distortion) {
		this.distortion = distortion;
	}

	@Mixin(EntityRenderer.class)
	private static class MixinEntityRenderer {

		@ModifyVariable(method = "setupCameraTransform", at = @At("STORE"), ordinal = 2)
		public float setPortalDistortion(float distortion) {
			return new RenderPortalDistortionEvent(distortion).fire().distortion;
		}

	}

}
