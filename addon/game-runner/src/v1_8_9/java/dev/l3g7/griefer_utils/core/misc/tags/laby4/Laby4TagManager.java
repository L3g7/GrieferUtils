/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.tags.laby4;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.tags.Tags;
import dev.l3g7.griefer_utils.core.misc.tags.Tags.SpecialBadge;
import dev.l3g7.griefer_utils.core.misc.tags.Tags.TagManager;
import net.labymod.api.Laby;
import net.labymod.api.LabyAPI;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.client.network.NetworkPlayerInfo;
import net.labymod.api.client.render.font.RenderableComponent;
import net.labymod.api.user.GameUserService;
import net.labymod.core.client.gui.screen.activity.activities.ingame.playerlist.PlayerListRenderer;
import net.labymod.core.main.LabyMod;
import net.labymod.core.main.user.badge.RankBadgeRenderer;
import net.labymod.serverapi.api.model.component.ServerAPIComponent;
import net.labymod.serverapi.core.model.display.Subtitle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.misc.tags.Tags.SpecialBadge.DEFAULT_BADGE;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.features.render.ClanTags.showSubtitle;
import static dev.l3g7.griefer_utils.features.uncategorized.settings.Badges.showBadges;

@Bridge
@Singleton
@ExclusiveTo(LABY_4)
public class Laby4TagManager implements TagManager {

	public static String icon = "icon";
	private final List<Subtitle> subtitles = new ArrayList<>();

	@Override
	public void setOnline(UUID uuid) { /* NO-OP */ }

	@Override
	public void setOffline(UUID uuid) { /* NO-OP */ }

	@Override
	public void toggleBadges(boolean enabled) { /* NO-OP */ }

	@Override
	public void setSubtitle(UUID uuid, String text, double scale) {
		Subtitle subtitle = Subtitle.create(uuid, ServerAPIComponent.text(text), scale);
		subtitles.add(subtitle);
		if (!showSubtitle())
			return;

		mc().addScheduledTask(() -> LabyMod.references().subtitleService().addSubtitle(subtitle));
	}

	@Override
	public void toggleSubtitles(boolean enabled) {
		for (Subtitle subtitle : subtitles) {
			if (enabled)
				LabyMod.references().subtitleService().addSubtitle(subtitle);
			else
				LabyMod.references().subtitleService().removeSubtitle(subtitle);
		}
	}

	/**
	 * Render player percentage in tablist
	 */
	@ExclusiveTo(LABY_4)
	@Mixin(value = PlayerListRenderer.class, remap = false)
	private static class MixinPlayerListRenderer {

		@Inject(method = "render", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
		private void injectRender(ScreenContext context, LabyAPI labyAPI, Bounds bounds, boolean update, CallbackInfo ci, int screenWidth, float columnsWidth, float backgroundWidth, RenderableComponent headerRenderableComponent, RenderableComponent footerRenderableComponent, int x, int y) {
			Tags.renderUserPercentage(x + backgroundWidth);
		}

	}

	/**
	 * Render badge in tablist
	 */
	@ExclusiveTo(LABY_4)
	@Mixin(value = RankBadgeRenderer.class, remap = false)
	private static class MixinRankBadgeRenderer {

		@Shadow
		@Final
		private GameUserService gameUserService;

		@Inject(method = "render", at = @At("HEAD"), cancellable = true)
		private void injectRender(ScreenContext context, float x, float y, NetworkPlayerInfo player, CallbackInfo ci) {
			UUID uuid = player.profile().getUniqueId();
			if (!showBadges() || !Tags.isOnline(uuid))
				return;

			SpecialBadge badge = Tags.getBadge(uuid).orElse(DEFAULT_BADGE);
			boolean revealFamiliarUsers = Boolean.TRUE.equals(Laby.labyAPI().config().multiplayer().tabList().labyModBadge().get());
			Tags.renderBadge(badge, icon, revealFamiliarUsers, x, y);

			ci.cancel();
		}

	}

}
