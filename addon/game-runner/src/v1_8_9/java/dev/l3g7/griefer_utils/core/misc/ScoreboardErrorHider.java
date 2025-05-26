/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc;

import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Suppresses IllegalArgumentExceptions in {@link Scoreboard#createTeam} and NullPointerExceptions in
 * {@link Scoreboard#removeObjective} caused by invalid packets.
 */
@Mixin(Scoreboard.class)
public class ScoreboardErrorHider {

	@Inject(method = "createTeam", at = @At("HEAD"), cancellable = true)
	public void injectCreateTeam(String name, CallbackInfoReturnable<ScorePlayerTeam> cir) {
		ScorePlayerTeam existingTeam = ((Scoreboard) (Object) this).getTeam(name);
		if (existingTeam != null) {
			cir.setReturnValue(existingTeam);
			cir.cancel();
		}
	}

	@Inject(method = "removeObjective", at = @At("HEAD"), cancellable = true)
	public void injectRemoveObjective(ScoreObjective objective, CallbackInfo ci) {
		if (objective == null)
			ci.cancel();
	}

}
