/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.badges.laby4;

import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.misc.badges.Badges;
import dev.l3g7.griefer_utils.core.misc.badges.Badges.SpecialBadge;
import net.labymod.api.Laby;
import net.labymod.api.LabyAPI;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.configuration.labymod.main.LabyConfig;
import net.labymod.api.laby3d.renderer.snapshot.Extras;
import net.labymod.api.user.GameUser;
import net.labymod.api.user.badge.ServerBadge;
import net.labymod.api.user.group.Group;
import net.labymod.core.client.render.state.entity.DefaultGameUserSnapshot;
import net.labymod.core.main.user.serverfeature.DefaultServerFeatureService;
import net.labymod.core.main.user.serverfeature.UserServerFeature;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.lang.reflect.Method;

import static dev.l3g7.griefer_utils.core.misc.badges.Badges.SpecialBadge.DEFAULT_BADGE;
import static dev.l3g7.griefer_utils.features.uncategorized.settings.Badges.showBadges;

public class GrieferUtilsGameUserSnapshot extends DefaultGameUserSnapshot {

	public final @Nullable SpecialBadge badge;

	public GrieferUtilsGameUserSnapshot(GameUser user, Extras extras, LabyAPI api) {
		this(user, ((DefaultServerFeatureService) Laby.references().serverFeatureService()).get().getUserFeature(user.getUniqueId()), extras, api.config(), getBadge(user));
	}

	private GrieferUtilsGameUserSnapshot(GameUser user, @Nullable UserServerFeature userServerFeature, Extras extras, LabyConfig config, @Nullable SpecialBadge badge) {
		super(user.isUsingLabyMod(), user.isLegacy(), user.visibleGroup(), user.displayColor(), userServerFeature == null ? null : userServerFeature.getCountryCode(),
			userServerFeature == null ? null : userServerFeature.getBadges().toArray(new ServerBadge[0]),
			DefaultGameUserSnapshotAccessor.grieferUtils$calculateNameTagOffset(user),
			userServerFeature == null ? null : userServerFeature.getSubtitle(), createGroupComponent(user, badge),
			DefaultGameUserSnapshotAccessor.grieferUtils$isFriend(user), config.ingame().showUserIndicatorBesideName().get(), config.ingame().showCountryFlag().get(), config.ingame().cosmetics().renderCosmetics().get(), extras);
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

		return DefaultGameUserSnapshotAccessor.grieferUtils$createGroupComponent(user.visibleGroup());
	}

	public static final Method isFriend = Reflection.getMethod(DefaultGameUserSnapshot.class, "isFriend", GameUser.class);
	public static final Method calculateNameTagOffset = Reflection.getMethod(DefaultGameUserSnapshot.class, "calculateNameTagOffset", GameUser.class);
	public static final Method createGroupComponent = Reflection.getMethod(DefaultGameUserSnapshot.class, "createGroupComponent", Group.class);

	static {
		isFriend.setAccessible(true);
		calculateNameTagOffset.setAccessible(true);
		createGroupComponent.setAccessible(true);
	}

	@Mixin(value = DefaultGameUserSnapshot.class, remap = false)
	public interface DefaultGameUserSnapshotAccessor {
		@Invoker("isFriend")
		static boolean grieferUtils$isFriend(GameUser user) {
			return Reflection.invoke(DefaultGameUserSnapshot.class, isFriend, user);
		}

		@Invoker("calculateNameTagOffset")
		static float grieferUtils$calculateNameTagOffset(GameUser user) {
			return Reflection.invoke(DefaultGameUserSnapshot.class, calculateNameTagOffset, user);
		}

		@Invoker("createGroupComponent")
		static Component grieferUtils$createGroupComponent(Group group) {
			return Reflection.invoke(DefaultGameUserSnapshot.class, createGroupComponent, group);
		}
	}

}
