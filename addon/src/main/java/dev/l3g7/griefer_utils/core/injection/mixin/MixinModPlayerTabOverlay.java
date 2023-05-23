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

package dev.l3g7.griefer_utils.core.injection.mixin;

import dev.l3g7.griefer_utils.features.uncategorized.settings.Badges;
import dev.l3g7.griefer_utils.misc.badges.GrieferUtilsGroup;
import net.labymod.core_implementation.mc18.gui.ModPlayerTabOverlay;
import net.labymod.main.LabyMod;
import net.labymod.main.ModSettings;
import net.labymod.user.User;
import net.minecraft.client.gui.Gui;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = ModPlayerTabOverlay.class, remap = false)
public class MixinModPlayerTabOverlay {

	private int left = -1;

	@Redirect(method = "newTabOverlay", at = @At(value = "FIELD", target = "Lnet/labymod/main/ModSettings;revealFamiliarUsers:Z", ordinal = 1), remap = false)
	private boolean redirectRevealFamiliarUsers(ModSettings instance) {
		return instance.revealFamiliarUsers || Badges.showBadges();
	}

	@Redirect(method = "newTabOverlay", at = @At(value = "INVOKE", target = "Lnet/labymod/user/User;isFamiliar()Z"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/labymod/core_implementation/mc18/gui/ModPlayerTabOverlay;drawRect(IIIII)V", ordinal = 0)))
	private boolean redirectIsFamiliar(User instance) {
		if (Badges.showBadges() && instance.getGroup() instanceof GrieferUtilsGroup)
			return true;

		return LabyMod.getSettings().revealFamiliarUsers && instance.isFamiliar();
	}

	@Redirect(method = "newTabOverlay", at = @At(value = "INVOKE", target = "Lnet/labymod/core_implementation/mc18/gui/ModPlayerTabOverlay;drawRect(IIIII)V", ordinal = 1), require = 1, remap = true)
	private void redirectDrawRect(int left, int top, int right, int bottom, int color) {
		this.left = left + 1;
		Gui.drawRect(left, top, right, bottom, color);
	}

	@Inject(method = "newTabOverlay", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
	public void injectNewTabOverlay(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn, CallbackInfo ci) {
		Badges.renderUserPercentage(left, width);
	}

}
