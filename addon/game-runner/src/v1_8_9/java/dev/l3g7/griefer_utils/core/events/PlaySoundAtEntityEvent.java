/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events;

import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

public class PlaySoundAtEntityEvent extends Event {

	public final Entity entity;

	public PlaySoundAtEntityEvent(Entity entity) {
		this.entity = entity;
	}

	@Mixin(EntityPlayerSP.class)
	private static class MixinEntityPlayerSP {

	    @Inject(method = "playSound", at = @At("HEAD"), cancellable = true)
	    public void injectPlaySound(String name, float volume, float pitch, CallbackInfo ci) {
	    	if (new PlaySoundAtEntityEvent(c(this)).fire().isCanceled())
				ci.cancel();
	    }

	}

	@Mixin(World.class)
	private static abstract class MixinWorld {

		@Inject(method = "playSoundAtEntity", at = @At("HEAD"), cancellable = true)
	    public void injectPlaySoundAtEntity(Entity entityIn, String name, float volume, float pitch, CallbackInfo ci) {
		    if (new PlaySoundAtEntityEvent(entityIn).fire().isCanceled())
			    ci.cancel();
	    }

		@Inject(method = "playSoundToNearExcept", at = @At("HEAD"), cancellable = true)
		public void injectPlaySoundToNearExcept(EntityPlayer player, String name, float volume, float pitch, CallbackInfo ci) {
			if (new PlaySoundAtEntityEvent(player).fire().isCanceled())
				ci.cancel();
		}

	}

}
