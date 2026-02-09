/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.badges.laby3;

import dev.l3g7.griefer_utils.core.misc.badges.Badges;
import dev.l3g7.griefer_utils.core.misc.badges.Badges.SpecialBadge;
import net.labymod.main.LabyMod;
import net.labymod.user.group.EnumGroupDisplayType;
import net.labymod.user.group.LabyGroup;

import java.util.Optional;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.misc.badges.Badges.SpecialBadge.DEFAULT_BADGE;
import static net.labymod.user.group.EnumGroupDisplayType.ABOVE_HEAD;
import static net.labymod.user.group.EnumGroupDisplayType.BESIDE_NAME;

public class GrieferUtilsGroup extends LabyGroup {

	private static final GrieferUtilsGroup DEFAULT_GROUP = new GrieferUtilsGroup(DEFAULT_BADGE);
	private static String icon = "icon";

	private final SpecialBadge badge;

	public static GrieferUtilsGroup from(UUID user) {
		Optional<SpecialBadge> badge = Badges.getBadge(user);
		return badge.map(GrieferUtilsGroup::new).orElse(DEFAULT_GROUP);
	}

	public static void setIcon(String icon) {
		GrieferUtilsGroup.icon = icon;
	}

	public static String getIcon() {
		return icon;
	}

	protected GrieferUtilsGroup(SpecialBadge badge) {
		this.badge = badge;
	}

	@Override
	public String getDisplayTag() {
		return "§fGrieferUtils " + badge.title();
	}

	@Override
	public void renderBadge(double x, double y, double width, double height, boolean small) {
		if (!dev.l3g7.griefer_utils.features.uncategorized.settings.Badges.showBadges()) {
			super.renderBadge(x, y, width, height, small);
			return;
		}

		Badges.renderBadge(badge, icon, LabyMod.getSettings().revealFamiliarUsers, x, y);
	}

	@Override
	public EnumGroupDisplayType getDisplayType() {
		return badge.title() == null ? BESIDE_NAME : ABOVE_HEAD;
	}
}
