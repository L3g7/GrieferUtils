/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc;

import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class CrosshairHider {

	private static boolean visible = true;

	public static boolean isVisible() {
		return visible;
	}

	public static void hide() {
		visible = false;
	}

	public static void show() {
		visible = true;
	}

	@Mixin(GuiIngame.class)
	private static class MixinGuiIngame {

	    @Inject(method = "showCrosshair", at = @At("HEAD"), cancellable = true)
	    private void injectShowCrosshair(CallbackInfoReturnable<Boolean> cir) {
	    	if (!isVisible())
				cir.setReturnValue(false);
	    }

	}

}
