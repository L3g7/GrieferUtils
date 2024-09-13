/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.injection.mixins;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import net.labymod.v1_8_9.client.component.VersionedStyle;
import net.labymod.v1_8_9.client.component.VersionedTextColor;
import net.minecraft.util.ChatStyle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

/**
 * Hopefully this class can soon be removed when LabyMod fixed their code
 */
@Mixin(ChatStyle.class)
@ExclusiveTo(LABY_4)
public class MixinChatStyle {

	@Inject(method = "createShallowCopy", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void injectCreateShallowCopy(CallbackInfoReturnable<ChatStyle> cir, ChatStyle style) {
		VersionedTextColor labyMod$color = Reflection.get(this, "labyMod$color");
		((VersionedStyle) style).setLabyColor(labyMod$color);
	}

	@Inject(method = "createDeepCopy", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void injectCreateDeepCopy(CallbackInfoReturnable<ChatStyle> cir, ChatStyle style) {
		VersionedTextColor labyMod$color = Reflection.get(this, "labyMod$color");
		((VersionedStyle) style).setLabyColor(labyMod$color);
	}

}
