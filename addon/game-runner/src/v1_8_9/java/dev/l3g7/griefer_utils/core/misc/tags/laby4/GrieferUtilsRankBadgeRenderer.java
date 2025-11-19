/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.tags.laby4;

import dev.l3g7.griefer_utils.core.misc.tags.Tags;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.network.NetworkPlayerInfo;
import net.labymod.core.main.user.serverfeature.badge.RankBadgeRenderer;

import java.util.UUID;

import static dev.l3g7.griefer_utils.core.misc.tags.Tags.SpecialBadge.DEFAULT_BADGE;
import static dev.l3g7.griefer_utils.core.misc.tags.laby4.Laby4TagManager.icon;
import static dev.l3g7.griefer_utils.features.uncategorized.settings.Badges.showBadges;

public class GrieferUtilsRankBadgeRenderer extends RankBadgeRenderer {

	@Override
	public void render(ScreenContext context, float x, float y, NetworkPlayerInfo player) {
		UUID uuid = player.profile().getUniqueId();
		if (!showBadges() || !Tags.isOnline(uuid)) {
			super.render(context, x, y, player);
			return;
		}

		Tags.SpecialBadge badge = Tags.getBadge(uuid).orElse(DEFAULT_BADGE);
		boolean revealFamiliarUsers = Boolean.TRUE.equals(Laby.labyAPI().config().multiplayer().tabList().labyModBadge().get());
		Tags.renderBadge(badge, icon, revealFamiliarUsers, x, y);
	}

}
