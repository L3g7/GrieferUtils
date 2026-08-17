/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.badges.laby4;

import dev.l3g7.griefer_utils.core.misc.badges.Badges;
import net.labymod.api.Laby;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.render.state.EntityExtraKeys;
import net.labymod.api.client.render.state.entity.EntitySnapshot;
import net.labymod.core.client.render.state.entity.mutable.LiveAvatarSnapshot;
import net.labymod.core.main.user.group.tag.GroupIconTag;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

import static dev.l3g7.griefer_utils.core.misc.badges.Badges.icon;

public class GrieferUtilsGroupIconTag extends GroupIconTag {

	private @Nullable Badges.SpecialBadge badge;

	@Override
	public void begin(EntitySnapshot snapshot) {
		if (snapshot instanceof GrieferUtilsGameUserSnapshot guSnapshot)
			badge = guSnapshot.badge;
		else if (snapshot instanceof LiveAvatarSnapshot avatarSnapshot) {
			if (avatarSnapshot.extras().get(EntityExtraKeys.GAME_USER) instanceof GrieferUtilsGameUserSnapshot guSnapshot)
				badge = guSnapshot.badge;
		}
		else
			badge = null;

		super.begin(snapshot);
	}

	@Override
	public Icon getIcon(EntitySnapshot snapshot) {
		if (badge != null)
			return Icon.texture((net.labymod.api.client.resources.ResourceLocation) new ResourceLocation("griefer_utils", "icons/high_res/" + icon + ".png"));

		return super.getIcon(snapshot);
	}

	@Override
	public int getColor(EntitySnapshot snapshot) {
		if (badge != null) {
			if (!icon.equals("icon"))
				return 0xFFFFFFFF;

			boolean revealFamiliarUsers = Boolean.TRUE.equals(Laby.labyAPI().config().multiplayer().tabList().labyModBadge().get());
			Color color = new Color(revealFamiliarUsers ? badge.colorWithLabymod() : badge.colorWithoutLabymod());
			return color.getRGB();
		}

		return super.getColor(snapshot);
	}

}
