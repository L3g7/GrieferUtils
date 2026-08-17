/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import net.labymod.accountmanager.storage.account.Account;
import net.labymod.api.event.client.session.SessionUpdateEvent;
import net.labymod.main.LabyMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

public class AccountSwitchEvent extends Event {

	@ExclusiveTo(LABY_3)
	@Mixin(LabyMod.class)
	private static class MixinLabyMod {

		@Inject(method = "setSession", at = @At("TAIL"), remap = false)
		public void injectSetSession(Account account, CallbackInfo ci) {
			new AccountSwitchEvent().fire();
		}

	}

	@ExclusiveTo(LABY_4)
	private static class Laby4Registrar {

		@OnEnable
		public static void registerEvents() {
			Laby4Util.register(SessionUpdateEvent.class, v -> new AccountSwitchEvent().fire());
		}

	}

}
