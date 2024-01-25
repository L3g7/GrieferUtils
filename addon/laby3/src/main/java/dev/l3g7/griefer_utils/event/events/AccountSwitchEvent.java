/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.event.events;

import dev.l3g7.griefer_utils.core.event_bus.Event;
import net.labymod.accountmanager.storage.account.Account;
import net.labymod.main.LabyMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class AccountSwitchEvent extends Event {

	@Mixin(LabyMod.class)
	private static class MixinLabyMod {

		@Inject(method = "setSession", at = @At("TAIL"), remap = false)
		public void injectSetSession(Account account, CallbackInfo ci) {
			new AccountSwitchEvent().fire();
		}

	}

}
