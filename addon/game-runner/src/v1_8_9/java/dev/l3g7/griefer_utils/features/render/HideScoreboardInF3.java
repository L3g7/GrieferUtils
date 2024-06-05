/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.render;

import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.ScoreObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
public class HideScoreboardInF3 extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Scoreboard bei F3 verstecken")
		.description("Versteckt das Scoreboard, wenn das F3-Menü geöffnet wurde.")
		.icon("wooden_board");

	@Mixin(GuiIngame.class)
	private static class MixinGuiIngame {

		@Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
		public void injectRenderScoreboard(ScoreObjective objective, ScaledResolution scaledRes, CallbackInfo ci) {
			if (mc().gameSettings.showDebugInfo && FileProvider.getSingleton(HideScoreboardInF3.class).isEnabled())
				ci.cancel();
		}

	}

}
