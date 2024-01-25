/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.event.events;

import dev.l3g7.griefer_utils.core.event_bus.Event;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class PlaySoundEvent extends Event {

	public final String name;

	public PlaySoundEvent(ISound sound) {
		this.name = sound.getSoundLocation().getResourcePath();
	}

	@Mixin(SoundManager.class)
	private static class MixinSoundManager {

		@Shadow
		private boolean loaded;

		@Inject(method = "playSound", at = @At("HEAD"), cancellable = true)
	    public void injectPlaySound(ISound p_sound, CallbackInfo ci) {
	    	if (loaded && new PlaySoundEvent(p_sound).fire().isCanceled())
				ci.cancel();
	    }

	}

}
