/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.badges.laby4;

import dev.l3g7.griefer_utils.core.misc.badges.Badges;
import dev.l3g7.griefer_utils.core.misc.badges.Badges.SpecialBadge;
import net.labymod.api.Laby;
import net.labymod.api.LabyAPI;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.component.format.TextDecoration;
import net.labymod.api.configuration.labymod.main.LabyConfig;
import net.labymod.api.laby3d.renderer.snapshot.Extras;
import net.labymod.api.labyconnect.LabyConnect;
import net.labymod.api.labyconnect.LabyConnectSession;
import net.labymod.api.user.GameUser;
import net.labymod.api.user.badge.ServerBadge;
import net.labymod.api.user.group.Group;
import net.labymod.api.user.group.GroupDisplayType;
import net.labymod.core.client.render.state.entity.DefaultGameUserSnapshot;
import net.labymod.core.main.LabyMod;
import net.labymod.core.main.user.DefaultGameUser;
import net.labymod.core.main.user.GameUserData;
import net.labymod.core.main.user.GameUserItem;
import net.labymod.core.main.user.serverfeature.UserServerFeature;
import net.labymod.core.main.user.shop.item.model.AttachmentPoint;
import org.jetbrains.annotations.Nullable;

import static dev.l3g7.griefer_utils.core.misc.badges.Badges.SpecialBadge.DEFAULT_BADGE;
import static dev.l3g7.griefer_utils.features.uncategorized.settings.Badges.showBadges;

public class GrieferUtilsGameUserSnapshot extends DefaultGameUserSnapshot {

	private static final Component PREFIX = Component.text("LABY", Style.builder().color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD).build());
	public final @Nullable SpecialBadge badge;

	public GrieferUtilsGameUserSnapshot(GameUser user, Extras extras, LabyAPI api) {
		this(user, LabyMod.references().serverFeatureService().get().getUserFeature(user.getUniqueId()), extras, api.config(), getBadge(user));
	}

	private GrieferUtilsGameUserSnapshot(GameUser user, @Nullable UserServerFeature userServerFeature, Extras extras, LabyConfig config, @Nullable SpecialBadge badge) {
		super(user.isUsingLabyMod(), user.isLegacy(), user.visibleGroup(), user.displayColor(), userServerFeature == null ? null : userServerFeature.getCountryCode(), userServerFeature == null ? null : userServerFeature.getBadges().toArray(new ServerBadge[0]), calculateNameTagOffset(user), userServerFeature == null ? null : userServerFeature.getSubtitle(), createGroupComponent(user, badge), isFriend(user), config.ingame().showUserIndicatorBesideName().get(), config.ingame().showCountryFlag().get(), config.ingame().cosmetics().renderCosmetics().get(), extras);
		this.badge = badge;
	}

	private static @Nullable SpecialBadge getBadge(GameUser user) {
		if (showBadges() && Badges.isOnline(user.getUniqueId()))
			return Badges.getBadge(user.getUniqueId()).orElse(DEFAULT_BADGE);

		return null;
	}

	private static Component createGroupComponent(GameUser user, SpecialBadge badge) {
		if (badge != null && badge != DEFAULT_BADGE) {
			return Component.text("GrieferUtils ")
				.color(TextColor.color(0xFFFFFF))
				.append(Component.text(badge.title()));
		}

		return createGroupComponent(user.visibleGroup());
	}

	/**
	 * Copied from {@link DefaultGameUserSnapshot} because it's private.
	 */
	private static Component createGroupComponent(Group group) {
		return group.getDisplayType() != GroupDisplayType.ABOVE_HEAD ? null : Component.text().append(PREFIX).append(Component.space()).append(Component.text(group.getTagName(), group.getTextColor())).build();
	}

	/**
	 * Copied from {@link DefaultGameUserSnapshot} because it's private.
	 */
	private static float calculateNameTagOffset(GameUser user) {
		if (user instanceof DefaultGameUser defaultGameUser && defaultGameUser.getUserData() instanceof GameUserData data) {
			float offset = 0.0F;
			for (GameUserItem entry : data.getItems())
				if (entry.item().itemDetails().getAttachmentPoint() == AttachmentPoint.HEAD && entry.item().canBeRendered())
					offset = Math.max(offset, entry.item().getNameTagOffset());

			if (offset > 0.0F)
				return (offset + 0.2F) / Laby.references().renderConstants().nameTagScale() - 1.0F;
		}
		return 0.0F;
	}

	/**
	 * Copied from {@link DefaultGameUserSnapshot} because it's private.
	 */
	private static boolean isFriend(GameUser user) {
		LabyConnect labyConnect = Laby.references().labyConnect();
		if (labyConnect.isAuthenticated())
			if (labyConnect.getSession() instanceof LabyConnectSession session)
				return session.getFriend(user.getUniqueId()) != null;

		return false;
	}

}
