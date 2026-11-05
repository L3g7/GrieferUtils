/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.badges.laby4;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.core.misc.badges.Badges;
import dev.l3g7.griefer_utils.core.misc.badges.Badges.SpecialBadge;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.entity.player.badge.BadgeRegistry;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.render.state.EntityExtraKeys;
import net.labymod.api.client.render.state.entity.EntitySnapshot;
import net.labymod.api.configuration.labymod.main.LabyConfig;
import net.labymod.api.laby3d.renderer.snapshot.Extras;
import net.labymod.api.laby3d.renderer.snapshot.LabySnapshot;
import net.labymod.api.user.GameUser;
import net.labymod.api.user.badge.ServerBadge;
import net.labymod.api.user.group.Group;
import net.labymod.api.util.CountryCode;
import net.labymod.core.main.user.serverfeature.badge.RankBadgeRenderer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.awt.*;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.misc.badges.Badges.SpecialBadge.DEFAULT_BADGE;
import static dev.l3g7.griefer_utils.core.misc.badges.Badges.icon;
import static dev.l3g7.griefer_utils.features.uncategorized.settings.Badges.showBadges;
import static net.labymod.api.client.entity.player.badge.PositionType.LEFT_TO_NAME;

/**
 * Fail-soft injection of GrieferUtils badges.
 */
public class BadgeInjection {

	public interface GrieferUtilsGameUserSnapshot {
		@Nullable SpecialBadge grieferUtils$getBadge();
	}

	@OnEnable
	private static void overrideBadgeRenderer() {
		BadgeRegistry badges = Laby.references().badgeRegistry();
		try {
			var renderer = new GrieferUtilsBadgeRenderer(new RankBadgeRenderer(false));
			badges.unregister("labymod_rank");
			badges.register("labymod_rank", LEFT_TO_NAME, renderer);
		} catch (Throwable t) {
			t.printStackTrace(System.err); // Fail softly
		}
	}

	/**
	 * Initialization
	 */
	@Pseudo
	@ExclusiveTo(LABY_4)
	@Mixin(targets = "net.labymod.core.client.render.state.entity.DefaultGameUserSnapshot", remap = false)
	public static abstract class DefaultGameUserSnapshotMixin implements GrieferUtilsGameUserSnapshot {

		@Unique
		private static @Nullable SpecialBadge grieferUtils$badgeCapture;

		@Unique
		private @Nullable SpecialBadge grieferUtils$badge;

		@Inject(
			method = "<init>(Lnet/labymod/api/user/GameUser;Lnet/labymod/core/main/user/serverfeature/UserServerFeature;Lnet/labymod/core/labyconnect/livestream/LiveStream;Lnet/labymod/core/main/user/partnertag/PartnerTag;Lnet/labymod/api/laby3d/renderer/snapshot/Extras;Lnet/labymod/api/configuration/labymod/main/LabyConfig;)V",
			at = @At("HEAD"),
			require = 0
		)
		private static void captureBadge(GameUser user, @Coerce Object userServerFeature, @Coerce Object liveStream, @Coerce Object partnerTag, Extras extras, LabyConfig config, CallbackInfo ci) {
			if (showBadges() && Badges.isOnline(user.getUniqueId()))
				grieferUtils$badgeCapture = Badges.getBadge(user.getUniqueId()).orElse(DEFAULT_BADGE);
			else
				grieferUtils$badgeCapture = null;
		}

		@Inject(
			method = "<init>(ZZLnet/labymod/api/user/group/Group;Lnet/labymod/api/client/component/format/TextColor;Lnet/labymod/api/util/CountryCode;Lnet/labymod/core/labyconnect/livestream/LiveStream;Lnet/labymod/core/main/user/partnertag/PartnerTag;[Lnet/labymod/api/user/badge/ServerBadge;FLnet/labymod/core/main/user/serverfeature/subtitle/SubtitleComponent;Lnet/labymod/api/client/component/Component;ZZZZIILnet/labymod/api/laby3d/renderer/snapshot/Extras;)V",
			at = @At("TAIL"),
			require = 0
		)
		private void storeBadge(boolean usingLabyMod, boolean legacy, Group group, TextColor displayColor, CountryCode countryCode, @Coerce Object liveStream, @Coerce Object partnerTag, ServerBadge[] serverBadges, float highestNameTagOffset, @Coerce Object subtitle, Component groupComponent, boolean friend, boolean showUserIndicatorBesideNames, boolean showCountryFlagBesideNames, boolean showCosmetics, int groupTier, int joinYear, Extras extras, CallbackInfo ci) {
			grieferUtils$badge = grieferUtils$badgeCapture;
			grieferUtils$badgeCapture = null;
		}

		@Override
		public @Nullable SpecialBadge grieferUtils$getBadge() {
			return grieferUtils$badge;
		}

		@ModifyArg(
			method = "<init>(Lnet/labymod/api/user/GameUser;Lnet/labymod/core/main/user/serverfeature/UserServerFeature;Lnet/labymod/core/labyconnect/livestream/LiveStream;Lnet/labymod/core/main/user/partnertag/PartnerTag;Lnet/labymod/api/laby3d/renderer/snapshot/Extras;Lnet/labymod/api/configuration/labymod/main/LabyConfig;)V",
			at = @At(value = "INVOKE", target = "Lnet/labymod/core/client/render/state/entity/DefaultGameUserSnapshot;<init>(ZZLnet/labymod/api/user/group/Group;Lnet/labymod/api/client/component/format/TextColor;Lnet/labymod/api/util/CountryCode;Lnet/labymod/core/labyconnect/livestream/LiveStream;Lnet/labymod/core/main/user/partnertag/PartnerTag;[Lnet/labymod/api/user/badge/ServerBadge;FLnet/labymod/core/main/user/serverfeature/subtitle/SubtitleComponent;Lnet/labymod/api/client/component/Component;ZZZZIILnet/labymod/api/laby3d/renderer/snapshot/Extras;)V"),
			require = 0,
			index = 10
		)
		private static @Nullable Component injectGroup(@Nullable Component groupComponent) {
			var badge = grieferUtils$badgeCapture;
			if (badge != null && badge != DEFAULT_BADGE) {
				return Component.text("GrieferUtils ")
					.color(TextColor.color(0xFFFFFF))
					.append(Component.text(badge.title()));
			}

			return groupComponent;
		}

	}

	/**
	 * Display name rendering.
	 */
	@Pseudo
	@ExclusiveTo(LABY_4)
	@Mixin(targets = "net.labymod.core.client.gui.screen.activity.activities.ingame.playerlist.PlayerListRenderer", remap = false)
	public static abstract class MixinPlayerListRenderer {

		@Inject(method = "renderLabyModStats", at = @At("TAIL"), require = 0, locals = LocalCapture.CAPTURE_FAILSOFT)
		private void renderLabyModStats(ScreenContext context, float backgroundWidth, int x, int y, CallbackInfo ci, int onlineCount, int userCount) {
			Laby4BadgeManager.renderStats(context, backgroundWidth, x, y, onlineCount, userCount);
		}

	}

	/**
	 * Display name rendering.
	 */
	@Pseudo
	@ExclusiveTo(LABY_4)
	@Mixin(targets = "net.labymod.core.main.user.group.tag.GroupIconTag", remap = false)
	public static abstract class MixinGroupIconTag {

		@Unique
		private @Nullable SpecialBadge grieferUtils$badge;

		@Inject(method = "begin", at = @At("HEAD"), require = 0)
		private void injectBegin(EntitySnapshot snapshot, CallbackInfo ci) {
			grieferUtils$badge = null;

			if (snapshot instanceof GrieferUtilsGameUserSnapshot guSnapshot)
				grieferUtils$badge = guSnapshot.grieferUtils$getBadge();
			else if (snapshot instanceof LabySnapshot avatarSnapshot && avatarSnapshot.extras().get(EntityExtraKeys.GAME_USER) instanceof GrieferUtilsGameUserSnapshot guSnapshot)
				grieferUtils$badge = guSnapshot.grieferUtils$getBadge();
		}

		@Inject(method = "getIcon", at = @At("HEAD"), cancellable = true, require = 0)
		private void injectGetIcon(EntitySnapshot snapshot, CallbackInfoReturnable<Icon> cir) {
			if (grieferUtils$badge != null)
				cir.setReturnValue(Icon.svg(Laby.references().resourceLocationFactory().create("griefer_utils", "icons/high_res/" + icon + ".svg")));
		}

		@Inject(method = "getColor", at = @At("HEAD"), cancellable = true, require = 0)
		private void injectGetColor(EntitySnapshot snapshot, CallbackInfoReturnable<Integer> cir) {
			if (grieferUtils$badge == null)
				return;

			if (!icon.equals("icon")) {
				cir.setReturnValue(0xFFFFFFFF);
				return;
			}

			boolean revealFamiliarUsers = Boolean.TRUE.equals(Laby.labyAPI().config().multiplayer().tabList().labyModBadge().get());
			Color color = new Color(revealFamiliarUsers ? grieferUtils$badge.colorWithLabymod() : grieferUtils$badge.colorWithoutLabymod());
			cir.setReturnValue(color.getRGB());
		}

		@Inject(method = "getScale", at = @At("HEAD"), cancellable = true, require = 0)
		private void injectGetScale(CallbackInfoReturnable<Float> cir) {
			if (grieferUtils$badge != null)
				cir.setReturnValue(1f);
		}

		@Inject(method = "getIconOffsetX", at = @At("HEAD"), cancellable = true, require = 0)
		private void injectGetIconOffsetX(CallbackInfoReturnable<Float> cir) {
			if (grieferUtils$badge != null)
				cir.setReturnValue(-1f);
		}
	}
}
