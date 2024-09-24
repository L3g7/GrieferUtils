/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.injection.mixins;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.api.util.Util;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;

@ExclusiveTo(LABY_3)
@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer {

	@Inject(method = "readFontTexture", at = @At("TAIL"))
	private void injectReadFontTexture(CallbackInfo ci) {
		Field field = Reflection.getField(getClass(), "offsetBold");
		if (field == null) // Optifine isn't installed
			return;

		try {
			field.set(this, 1.0f);
		} catch (IllegalAccessException e) {
			throw Util.elevate(e);
		}
	}

}
