/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.events;

import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import net.labymod.api.Laby;
import net.labymod.api.user.group.Group;
import net.labymod.core.main.user.DefaultGameUser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;

public class UserSetGroupEvent extends Event {

	public final DefaultGameUser user;
	public final Group group;

	public UserSetGroupEvent(DefaultGameUser user, Group group) {
		this.user = user;
		this.group = group;
	}

	@Mixin(value = DefaultGameUser.class, remap = false)
	private static class MixinDefaultGameUser {

		@Inject(method = "setVisibleGroup", at = @At("HEAD"), cancellable = true)
		private void injectSetVisibleGroup(int identifier, CallbackInfo ci) {
			Group group = Laby.references().groupService().getGroup(identifier);
			if (new UserSetGroupEvent(c(this), group).fire().isCanceled())
				ci.cancel();
		}

	}

}
