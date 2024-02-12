/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.misc.badges;


import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.network.TabListEvent.TabListClearEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.TabListEvent.TabListPlayerAddEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.TabListEvent.TabListPlayerRemoveEvent;
import dev.l3g7.griefer_utils.v1_8_9.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.v1_8_9.misc.server.GUClient;
import net.labymod.api.Laby;
import net.labymod.api.Textures;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.configuration.labymod.main.laby.multiplayer.TabListConfig;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ResourceLocation;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;
import static java.lang.Boolean.TRUE;

public class Badges {

	static final SwitchSetting showPercentage = SwitchSetting.create()
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
			§aGrün§r: Supporter
			§eGelb§r: Unterstützer""")
		.config("settings.badges")
		.icon("icon")
		.defaultValue(true)
		.subSettings(showPercentage)
		.callback(b -> {
			if (!b) {
				BadgeManager.clearUsers();
				return;
			}

			if (mc().getNetHandler() == null)
				return;

			for (NetworkPlayerInfo info : mc().getNetHandler().getPlayerInfoMap())
				BadgeManager.queueUser(info.getGameProfile().getId());
		});

	public static boolean showBadges() {
		return enabled.get();
	}

	@EventListener
	private static void onTabListAddEvent(TabListPlayerAddEvent event) {
		if (enabled.get())
			BadgeManager.queueUser(event.data.getProfile().getId());
	}

	@EventListener
	private static void onTabListRemoveEvent(TabListPlayerRemoveEvent event) {
		if (enabled.get())
			BadgeManager.removeUser(event.data.getProfile().getId());
	}

	@EventListener
	private static void onTabListClearAddEvent(TabListClearEvent event) {
		if (enabled.get())
			BadgeManager.clearUsers();
	}

	public static void renderUserPercentage(int left, int width) {
		if (!showBadges() || !showPercentage.get())
			return;

		int x = width + left;

		int totalCount = mc().getNetHandler().getPlayerInfoMap().size();
		int familiarCount = 0;

		for (NetworkPlayerInfo npi : mc().getNetHandler().getPlayerInfoMap())
			if (Laby.references().gameUserService().gameUser(npi.getGameProfile().getId()).isUsingLabyMod())
				familiarCount++;

		TabListConfig tlc = Laby.labyAPI().config().multiplayer().tabList();

		if (TRUE.equals(tlc.labyModBadge().get()) && TRUE.equals(tlc.labyModPercentage().get())) {
			int percent = totalCount == 0 ? 0 : (int) Math.round(familiarCount / (double) totalCount * 100);
			String labyModText = String.format("§7%d§8/§7%d §a%d%%", familiarCount, totalCount, percent);
			double delta = Laby.references().renderPipeline().textRenderer().width(labyModText) * 0.7 + 6.5;
			x -= delta;

			Textures.SpriteLabyMod.DEFAULT_WOLF_HIGH_RES.render(Stack.getDefaultEmptyStack(), x, 1.5f, 6.5f);
			x -= 3f;
		}


		familiarCount = 0;
		for (NetworkPlayerInfo npi : mc().getNetHandler().getPlayerInfoMap())
			if (Laby.references().gameUserService().gameUser(npi.getGameProfile().getId()).visibleGroup() instanceof GrieferUtilsGroup)
				familiarCount++;

		int percent = totalCount == 0 ? 0 : (int) Math.round(familiarCount / (double) totalCount * 100);
		String text = GUClient.get().isAvailable() ? String.format("§7%d§8/§7%d §a%d%%", familiarCount, totalCount, percent) : "§c?";
		DrawUtils.drawRightString(text, x, 1.5, 0.7);

		DrawUtils.bindTexture(new ResourceLocation("griefer_utils", "icons/icon.png"));
		x -= Laby.references().renderPipeline().textRenderer().width(text) * 0.7;
		DrawUtils.drawTexture(x - 8, 1.25, 256, 256, 7, 7);
	}

}