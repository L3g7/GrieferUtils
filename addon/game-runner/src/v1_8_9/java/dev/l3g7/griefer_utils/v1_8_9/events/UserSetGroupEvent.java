/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.events;

import dev.l3g7.griefer_utils.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import net.labymod.api.Laby;
import net.labymod.api.user.group.Group;
import net.labymod.core.main.user.DefaultGameUser;
import net.labymod.user.User;
import net.labymod.user.group.LabyGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;

public class UserSetGroupEvent extends Event {

	public final Object user;
	public final Object group;

	public UserSetGroupEvent(Object user, Object group) {
		this.user = user;
		this.group = group;
	}

	@Mixin(value = DefaultGameUser.class, remap = false)
	@ExclusiveTo(LABY_4)
	private static class MixinDefaultGameUser {

		@Inject(method = "setVisibleGroup", at = @At("HEAD"), cancellable = true)
		private void injectSetVisibleGroup(int identifier, CallbackInfo ci) {
			Group group = Laby.references().groupService().getGroup(identifier);
			if (new UserSetGroupEvent(c(this), group).fire().isCanceled())
				ci.cancel();
		}

	}

	@Mixin(value = User.class, remap = false)
	@ExclusiveTo(LABY_3)
	private static class MixinUser {

		@Inject(method = "setGroup", at = @At("HEAD"), cancellable = true)
		public void injectSetGroup(LabyGroup group, CallbackInfo ci) {
			if (new UserSetGroupEvent(this, group).fire().isCanceled())
				ci.cancel();
		}

	}

}
