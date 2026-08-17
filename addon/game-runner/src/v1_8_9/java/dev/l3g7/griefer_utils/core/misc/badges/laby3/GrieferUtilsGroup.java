/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.badges.laby3;

import dev.l3g7.griefer_utils.core.misc.badges.Badges;
import dev.l3g7.griefer_utils.core.misc.badges.Badges.SpecialBadge;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import net.labymod.main.LabyMod;
import net.labymod.user.group.EnumGroupDisplayType;
import net.labymod.user.group.LabyGroup;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Optional;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.misc.badges.Badges.SpecialBadge.DEFAULT_BADGE;
import static dev.l3g7.griefer_utils.core.misc.badges.Badges.icon;
import static net.labymod.user.group.EnumGroupDisplayType.ABOVE_HEAD;
import static net.labymod.user.group.EnumGroupDisplayType.BESIDE_NAME;

public class GrieferUtilsGroup extends LabyGroup {

	private static final GrieferUtilsGroup DEFAULT_GROUP = new GrieferUtilsGroup(DEFAULT_BADGE);

	private final SpecialBadge badge;

	public static GrieferUtilsGroup from(UUID user) {
		Optional<SpecialBadge> badge = Badges.getBadge(user);
		return badge.map(GrieferUtilsGroup::new).orElse(DEFAULT_GROUP);
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

		if (icon.equals("icon")) {
			Color color = new Color(LabyMod.getSettings().revealFamiliarUsers ? badge.colorWithLabymod() : badge.colorWithoutLabymod());
			GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1);
		}

		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		DrawUtils.bindTexture(new ResourceLocation("griefer_utils", "icons/high_res/" + icon + ".png"));
		DrawUtils.drawTexture(x, y, 255, 255, 8, 8, 1.1f);
		GlStateManager.color(1, 1, 1, 1);
	}

	@Override
	public EnumGroupDisplayType getDisplayType() {
		return badge.title() == null ? BESIDE_NAME : ABOVE_HEAD;
	}
}
