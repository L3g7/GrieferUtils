/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.injection.mixins;

import net.labymod.core.localization.DefaultInternationalization;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DefaultInternationalization.class, remap = false)
public class MixinDefaultInternationalization {

	@Inject(method = "getRawTranslation", at = @At("HEAD"), cancellable = true)
	private void injectGetRawTranslation(String key, CallbackInfoReturnable<String> cir) {
		if (key.equals("labymod.ui.keybind.none"))
			cir.setReturnValue("§8[Keine Taste hinterlegt]");
	}

}
