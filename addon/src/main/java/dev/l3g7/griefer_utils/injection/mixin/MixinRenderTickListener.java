/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.injection.mixin;

import net.labymod.main.LabyMod;
import net.labymod.main.listeners.RenderTickListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderTickListener.class)
public class MixinRenderTickListener {

	/**
	 * Fix custom achievements not being rendered when in a gui in game.
	 */
	@Redirect(method = "drawMenuOverlay", at = @At(value = "INVOKE", target = "Lnet/labymod/main/LabyMod;isInGame()Z"), remap = false)
	public boolean redirectIsInGame(LabyMod instance) {
		return false;
	}

}
