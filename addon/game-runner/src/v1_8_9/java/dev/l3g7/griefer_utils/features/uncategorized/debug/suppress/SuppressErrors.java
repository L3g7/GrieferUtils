/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.debug.suppress;

import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import net.labymod.user.cosmetic.geometry.effect.effects.GeometryColor;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class SuppressErrors {

	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("Vanilla-Fehler unterdrücken")
		.description("Unterdrückt:",
			"- NullPointerExceptions in NetHandlerPlayClient#handleTeams",
			"- NumberFormatExceptions in GeometryColor#parse")
		.config("settings.automatic_bug_reporting.suppress_errors")
		.icon("XZRF:bug")
		.defaultValue(true);

	@Mixin(value = GeometryColor.class, remap = false)
	public static abstract class GeometryColorFormatSuppressor {

		@Inject(method = "parse", at = @At("HEAD"), cancellable = true)
		public void on(CallbackInfoReturnable<Boolean> cir) {
			if (!enabled.get())
				return;

			try {
				//noinspection DataFlowIssue
				Integer.parseInt(((GeometryColor) (Object) this).getArgs()[1]);
			} catch (NumberFormatException e) {
				cir.setReturnValue(false);
			}
		}

	}

	@Mixin(NetHandlerPlayClient.class)
	public static class TeamsNPESuppressor {

		@Shadow
		private WorldClient clientWorldController;

		@Inject(method = "handleTeams", at = @At("HEAD"), cancellable = true)
		public void on(S3EPacketTeams packet, CallbackInfo ci) {
			if (!enabled.get())
				return;

			Scoreboard scoreboard = this.clientWorldController.getScoreboard();
			if (packet.getAction() == 1 /* REMOVE_TEAM */)
				if (scoreboard.getTeam(packet.getName()) == null)
					ci.cancel();
		}

	}

}
