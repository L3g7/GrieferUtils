/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.misc.badges;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.network.TabListEvent.TabListClearEvent;
import dev.l3g7.griefer_utils.event.events.network.TabListEvent.TabListPlayerAddEvent;
import dev.l3g7.griefer_utils.event.events.network.TabListEvent.TabListPlayerRemoveEvent;
import dev.l3g7.griefer_utils.misc.server.GUClient;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.core_implementation.mc18.gui.ModPlayerTabOverlay;
import net.labymod.main.LabyMod;
import net.labymod.main.ModSettings;
import net.labymod.user.User;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

public class Badges {

	static final BooleanSetting showPercentage = new BooleanSetting()
		.name("Nutzer-Prozentsatz anzeigen")
		.description("Zeigt über der Tabliste an, wie viel Prozent der Spieler GrieferUtils benutzen.")
		.config("settings.badges")
		.icon("icon")
		.defaultValue(true);

	public static final BooleanSetting enabled = new BooleanSetting()
		.name("GrieferUtils-\nNutzer-Anzeige")
		.description("Zeigt vor den Namen von Spielern ein GrieferUtils-Icon an, wenn sie das Addon benutzen.")
		.config("settings.badges")
		.icon("icon")
		.defaultValue(true)
		.subSettings(showPercentage)
		.callback(b -> {
			if (!b) {
				GrieferUtilsUserManager.clearUsers();
				return;
			}

			if (mc().getNetHandler() == null)
				return;

			for (NetworkPlayerInfo info : mc().getNetHandler().getPlayerInfoMap())
				GrieferUtilsUserManager.queueUser(info.getGameProfile().getId());
		});

	public static boolean showBadges() {
		return enabled.get();
	}

	@EventListener
	private static void onTabListAddEvent(TabListPlayerAddEvent event) {
		if (enabled.get())
			GrieferUtilsUserManager.queueUser(event.data.getProfile().getId());
	}

	@EventListener
	private static void onTabListRemoveEvent(TabListPlayerRemoveEvent event) {
		if (enabled.get())
			GrieferUtilsUserManager.removeUser(event.data.getProfile().getId());
	}

	@EventListener
	private static void onTabListClearAddEvent(TabListClearEvent event) {
		if (enabled.get())
			GrieferUtilsUserManager.clearUsers();
	}

	public static void renderUserPercentage(int left, int width) {
		if (!showBadges() || !showPercentage.get())
			return;

		int x = width - left;

		int totalCount = mc().getNetHandler().getPlayerInfoMap().size();
		int familiarCount = 0;

		for (NetworkPlayerInfo npi : mc().getNetHandler().getPlayerInfoMap())
			if (labyMod().getUserManager().getUser(npi.getGameProfile().getId()).isFamiliar())
				familiarCount++;

		if (LabyMod.getSettings().revealFamiliarUsers && LabyMod.getSettings().revealFamiliarUsersPercentage) {
			int percent = totalCount == 0 ? 0 : (int) Math.round(familiarCount / (double) totalCount * 100);
			String labyModText = String.format("§7%d§8/§7%d §a%d%%", familiarCount, totalCount, percent);
			double delta = drawUtils().getStringWidth(labyModText) * 0.7 + 14;
			x -= delta;

			drawUtils().bindTexture("labymod/textures/labymod_logo.png");
			drawUtils().drawTexture(x + 6, 2.25, 256, 256, 6.5, 6.5);
		}

		familiarCount = 0;
		for (NetworkPlayerInfo npi : mc().getNetHandler().getPlayerInfoMap())
			if (labyMod().getUserManager().getUser(npi.getGameProfile().getId()).getGroup() instanceof GrieferUtilsGroup)
				familiarCount++;

		int percent = totalCount == 0 ? 0 : (int) Math.round(familiarCount / (double) totalCount * 100);
		String text = GUClient.get().isAvailable() ? String.format("§7%d§8/§7%d §a%d%%", familiarCount, totalCount, percent) : "§c?";
		drawUtils().drawRightString(text, x, 3, 0.7);

		drawUtils().bindTexture("griefer_utils/icons/icon.png");
		x -= drawUtils().getStringWidth(text) * 0.7;
		drawUtils().drawTexture(x - 8, 2.25, 256, 256, 7, 7);
	}

	@Mixin(value = ModPlayerTabOverlay.class, remap = false)
	private static class MixinModPlayerTabOverlay {

		private int left = -1;

		@Redirect(method = "newTabOverlay", at = @At(value = "FIELD", target = "Lnet/labymod/main/ModSettings;revealFamiliarUsers:Z", ordinal = 1), remap = false)
		private boolean redirectRevealFamiliarUsers(ModSettings instance) {
			return instance.revealFamiliarUsers || showBadges();
		}

		@Redirect(method = "newTabOverlay", at = @At(value = "INVOKE", target = "Lnet/labymod/user/User;isFamiliar()Z"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/labymod/core_implementation/mc18/gui/ModPlayerTabOverlay;drawRect(IIIII)V", ordinal = 0)))
		private boolean redirectIsFamiliar(User instance) {
			if (showBadges() && instance.getGroup() instanceof GrieferUtilsGroup)
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
			renderUserPercentage(left, width);
		}

	}

}
