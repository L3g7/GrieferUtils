/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.render;

import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Singleton
public class HideEffectParticles extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Effekt-Partikel verstecken")
		.description("Versteckt von Entities mit Effekten ausgelöste Partikel.")
		.icon("green_particle");

	@Mixin(EntityLivingBase.class)
	private static class MixinEntityLivingBase {

	    @Inject(method = "updatePotionEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/DataWatcher;getWatchableObjectInt(I)I"), cancellable = true)
	    private void injectUpdatePotionEffects(CallbackInfo ci) {
	    	if (FileProvider.getSingleton(HideEffectParticles.class).isEnabled())
				ci.cancel();
	    }

	}

}
