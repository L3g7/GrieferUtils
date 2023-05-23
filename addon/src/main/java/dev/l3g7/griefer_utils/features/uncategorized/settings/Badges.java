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

package dev.l3g7.griefer_utils.features.uncategorized.settings;

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.TabListEvent;
import dev.l3g7.griefer_utils.event.events.network.TabListEvent.TabListClearEvent;
import dev.l3g7.griefer_utils.event.events.network.TabListEvent.TabListPlayerAddEvent;
import dev.l3g7.griefer_utils.event.events.network.TabListEvent.TabListPlayerRemoveEvent;
import dev.l3g7.griefer_utils.misc.badges.GrieferUtilsGroup;
import dev.l3g7.griefer_utils.misc.badges.GrieferUtilsUserManager;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.main.LabyMod;
import net.minecraft.client.network.NetworkPlayerInfo;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

public class Badges {

	static final BooleanSetting showPercentage = new BooleanSetting()
		.name("Nutzer-Prozentsatz anzeigen")
		.description("Zeigt über der Tabliste an, wie viel Prozent der Spieler GrieferUtils benutzen")
		.config("settings.badges")
		.icon("icon")
		.defaultValue(true);

	static final BooleanSetting enabled = new BooleanSetting()
		.name("GrieferUtils Nutzer-Anzeige")
		.description("Setzt vor den Namen von GrieferUtils ein Icon")
		.config("settings.badges")
		.icon("icon")
		.defaultValue(true)
		.subSettings(showPercentage)
		.callback(b -> {
			if (!b) {
				GrieferUtilsUserManager.clearUsers();
				return;
			}

			for (NetworkPlayerInfo info : mc().getNetHandler().getPlayerInfoMap())
				GrieferUtilsUserManager.queueUser(info.getGameProfile().getId());
		});

	public static boolean showBadges() {
		return enabled.get();
	}

	@EventListener
	private static void onTabListAddEvent(TabListEvent event) {
		if (event instanceof TabListPlayerAddEvent)
			GrieferUtilsUserManager.queueUser(((TabListPlayerAddEvent) event).data.getProfile().getId());

		if (event instanceof TabListPlayerRemoveEvent)
			GrieferUtilsUserManager.removeUser(((TabListPlayerRemoveEvent) event).data.getProfile().getId());

		if (event instanceof TabListClearEvent)
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
		String text = String.format("§7%d§8/§7%d §a%d%%", familiarCount, totalCount, percent);
		drawUtils().drawRightString(text, x, 3, 0.7);

		drawUtils().bindTexture("griefer_utils/icons/icon.png");
		x -= drawUtils().getStringWidth(text) * 0.7;
		drawUtils().drawTexture(x - 8, 2.25, 256, 256, 7, 7);
	}

}
