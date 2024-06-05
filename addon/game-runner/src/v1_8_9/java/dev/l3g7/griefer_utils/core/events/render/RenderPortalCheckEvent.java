/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events.render;

import dev.l3g7.griefer_utils.core.api.event.event_bus.Event;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class RenderPortalCheckEvent extends Event {

	@Mixin(GuiIngame.class)
	private static class MixinGuiIngame {

		@Inject(method = "renderPortal", at = @At("HEAD"), cancellable = true)
		public void injectRenderPortal(float timeInPortal, ScaledResolution scaledRes, CallbackInfo ci) {
			if (new RenderPortalCheckEvent().fire().isCanceled())
				ci.cancel();
		}

	}

}
