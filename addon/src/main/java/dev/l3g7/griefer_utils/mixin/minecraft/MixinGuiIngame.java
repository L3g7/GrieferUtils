/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.mixin.minecraft;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.features.player.scoreboard.ScoreboardHandler;
import dev.l3g7.griefer_utils.features.render.HideScoreboardInF3;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.ScoreObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {

	@Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
	public void injectRenderScoreboard(ScoreObjective objective, ScaledResolution scaledRes, CallbackInfo ci) {
		if (mc().gameSettings.showDebugInfo && FileProvider.getSingleton(HideScoreboardInF3.class).isEnabled())
			ci.cancel();
	}

	@ModifyConstant(method = "renderScoreboard", constant = @Constant(intValue = 15), remap = false)
	private int modifyMaxScoreboardSize(int listSize) {
		if (ScoreboardHandler.shouldUnlockScoreboard())
			return Integer.MAX_VALUE;

		return listSize;
	}

}
