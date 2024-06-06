/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.badges;


import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.network.TabListEvent.TabListClearEvent;
import dev.l3g7.griefer_utils.core.events.network.TabListEvent.TabListPlayerAddEvent;
import dev.l3g7.griefer_utils.core.events.network.TabListEvent.TabListPlayerRemoveEvent;
import net.minecraft.client.network.NetworkPlayerInfo;

import static dev.l3g7.griefer_utils.core.misc.badges.BadgeManagerBridge.badgeManager;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class Badges {

	public static final SwitchSetting showPercentage = SwitchSetting.create()
		.name("Nutzer-Prozentsatz anzeigen")
		.description("Zeigt über der Tabliste an, wie viel Prozent der Spieler GrieferUtils benutzen.")
		.config("settings.badges")
		.icon("icon")
		.defaultValue(true);

	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("GrieferUtils-\nNutzer-Anzeige")
		.description("""
			Zeigt vor den Namen von Spielern ein GrieferUtils-Icon an, wenn sie das Addon benutzen.

			§nFarben:§r
			§cRot§r: Offizieller Account / Entwickler
			§bBlau§r: Entwickler
			§aGrün§r: Supporter""")
		.config("settings.badges")
		.icon("icon")
		.defaultValue(true)
		.subSettings(showPercentage)
		.callback(b -> {
			if (!b) {
				badgeManager.clearUsers();
				return;
			}

			if (mc().getNetHandler() == null)
				return;

			for (NetworkPlayerInfo info : mc().getNetHandler().getPlayerInfoMap())
				badgeManager.queueUser(info.getGameProfile().getId());
		});

	public static boolean showBadges() {
		return enabled.get();
	}

	@EventListener
	private static void onTabListAddEvent(TabListPlayerAddEvent event) {
		if (enabled.get())
			badgeManager.queueUser(event.data.getProfile().getId());
	}

	@EventListener
	private static void onTabListRemoveEvent(TabListPlayerRemoveEvent event) {
		if (enabled.get())
			badgeManager.removeUser(event.data.getProfile().getId());
	}

	@EventListener
	private static void onTabListClearAddEvent(TabListClearEvent event) {
		if (enabled.get())
			badgeManager.clearUsers();
	}

}