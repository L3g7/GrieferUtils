/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.badges.laby4;

import dev.l3g7.griefer_utils.core.misc.badges.Badges;
import net.labymod.api.Laby;
import net.labymod.api.client.entity.player.badge.renderer.BadgeRenderer;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.network.NetworkPlayerInfo;
import net.labymod.api.configuration.labymod.main.laby.multiplayer.TabListConfig;
import net.labymod.api.configuration.labymod.main.laby.multiplayer.TabListConfig.LabyModBadgePosition;
import net.labymod.api.user.GameUserService;

import java.util.UUID;

import static dev.l3g7.griefer_utils.core.misc.badges.Badges.SpecialBadge.DEFAULT_BADGE;
import static dev.l3g7.griefer_utils.core.misc.badges.Badges.icon;
import static dev.l3g7.griefer_utils.features.uncategorized.settings.Badges.showBadges;

/**
 * Tablist rendering.
 * @implNote based on Laby Client v49's RankBadgeRenderer.
 */
public class GrieferUtilsBadgeRenderer extends BadgeRenderer {

    private final GameUserService gameUserService = Laby.references().gameUserService();
	private final BadgeRenderer originalRenderer;

    public GrieferUtilsBadgeRenderer(BadgeRenderer originalRenderer) {
		this.originalRenderer = originalRenderer;
    }

	@Override
	public void render(ScreenContext context, float x, float y, NetworkPlayerInfo player) {
		UUID uuid = player.profile().getUniqueId();
		if (!showBadges() || !Badges.isOnline(uuid)) {
			originalRenderer.render(context, x, y, player);
			return;
		}

		Badges.SpecialBadge badge = Badges.getBadge(uuid).orElse(DEFAULT_BADGE);
		boolean revealFamiliarUsers = Boolean.TRUE.equals(Laby.labyAPI().config().multiplayer().tabList().labyModBadge().get());
		int color = 0xFFFFFFFF;
		if (icon.equals("icon"))
			color = (revealFamiliarUsers ? badge.colorWithLabymod() : badge.colorWithoutLabymod()) | 0xFF000000;

		Icon lmIcon = Icon.svg(Laby.references().resourceLocationFactory().create("griefer_utils", "icons/high_res/" + icon + ".svg"));
		context.canvas().submitIcon(lmIcon, x, y, 8.0F, 8.0F, false, color);
	}

	@Override
    public boolean isVisible(NetworkPlayerInfo player) {
        var position = Laby.labyAPI().config().multiplayer().tabList().labyModBadgePosition().get();
		return position == LabyModBadgePosition.BEFORE_NAME_SPACED || this.gameUserService.gameUser(player.profile().getUniqueId()).isUsingLabyMod();
	}

	@Override
    public int getSize(NetworkPlayerInfo player) {
        return 7;
    }

	@Override
    protected boolean begin(ScreenContext context) {
        TabListConfig tabList = Laby.labyAPI().config().multiplayer().tabList();
        if (!(Boolean) tabList.labyModBadge().get())
            return false;
        else
            return tabList.labyModBadgePosition().get() != LabyModBadgePosition.BEFORE_PING;
    }

}
