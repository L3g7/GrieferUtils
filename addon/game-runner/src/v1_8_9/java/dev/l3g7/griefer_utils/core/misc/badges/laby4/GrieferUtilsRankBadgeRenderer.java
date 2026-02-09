/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.badges.laby4;

import dev.l3g7.griefer_utils.core.misc.badges.Badges;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.network.NetworkPlayerInfo;
import net.labymod.core.main.user.serverfeature.badge.RankBadgeRenderer;

import java.util.UUID;

import static dev.l3g7.griefer_utils.core.misc.badges.Badges.SpecialBadge.DEFAULT_BADGE;
import static dev.l3g7.griefer_utils.core.misc.badges.laby4.Laby4BadgeManager.icon;
import static dev.l3g7.griefer_utils.features.uncategorized.settings.Badges.showBadges;

public class GrieferUtilsRankBadgeRenderer extends RankBadgeRenderer {

	@Override
	public void render(ScreenContext context, float x, float y, NetworkPlayerInfo player) {
		UUID uuid = player.profile().getUniqueId();
		if (!showBadges() || !Badges.isOnline(uuid)) {
			super.render(context, x, y, player);
			return;
		}

		Badges.SpecialBadge badge = Badges.getBadge(uuid).orElse(DEFAULT_BADGE);
		boolean revealFamiliarUsers = Boolean.TRUE.equals(Laby.labyAPI().config().multiplayer().tabList().labyModBadge().get());
		Badges.renderBadge(badge, icon, revealFamiliarUsers, x, y);
	}

}
