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
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.core.misc.tags.Tags;
import dev.l3g7.griefer_utils.core.misc.tags.Tags.TagManager;
import net.labymod.api.Laby;
import net.labymod.api.LabyAPI;
import net.labymod.api.client.entity.player.badge.BadgeRegistry;
import net.labymod.api.client.entity.player.badge.PositionType;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.client.render.font.RenderableComponent;
import net.labymod.api.client.render.state.entity.GameUserSnapshot;
import net.labymod.api.laby3d.renderer.snapshot.Extras;
import net.labymod.api.user.GameUser;
import net.labymod.api.user.group.Group;
import net.labymod.core.client.gui.screen.activity.activities.ingame.playerlist.PlayerListRenderer;
import net.labymod.core.client.render.state.entity.GameUserSnapshotFactory;
import net.labymod.core.main.LabyMod;
import net.labymod.core.main.user.group.tag.GroupTextTag;
import net.labymod.core.main.user.serverfeature.ServerFeature;
import net.labymod.core.main.user.serverfeature.subtitle.SubtitleComponent;
import net.labymod.serverapi.api.model.component.ServerAPIComponent;
import net.labymod.serverapi.core.model.display.Subtitle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.features.render.ClanTags.showSubtitle;
import static net.labymod.api.client.entity.player.tag.PositionType.LEFT_TO_NAME;

@Bridge
@Singleton
@ExclusiveTo(LABY_4)
public class Laby4TagManager implements TagManager {

	public static Group GRIEFERUTILS_GROUP = new Group(99, "grieferutils", "GrieferUtils", "FFFFFF", 'f', "", "", false);

	public static String icon = "icon";
	private final List<Subtitle> subtitles = new ArrayList<>();

	@OnEnable
	private void overrideBadgeRenderer() {
		BadgeRegistry badges = Laby.references().badgeRegistry();
		badges.unregister("labymod_rank");
		badges.register("labymod_rank", PositionType.LEFT_TO_NAME, new GrieferUtilsRankBadgeRenderer());

		var tags = Laby.references().tagRegistry();
		tags.unregister("labymod_icon_role");
		tags.register("labymod_icon_role", LEFT_TO_NAME, new GrieferUtilsGroupIconTag());
	}

	@Override
	public void setOnline(UUID uuid) { /* NO-OP */ }

	@Override
	public void setOffline(UUID uuid) { /* NO-OP */ }

	@Override
	public void toggleBadges(boolean enabled) { /* NO-OP */ }

	@Override
	public void setSubtitle(UUID uuid, String text, double scale) {
		Subtitle subtitle = Subtitle.create(uuid, text == null ? null : ServerAPIComponent.text(text), scale);
		subtitles.add(subtitle);
		if (!showSubtitle())
			return;

		mc().addScheduledTask(() -> LabyMod.references().serverFeatureService().get()
			.getOrCreateUserFeature(uuid).setSubtitle(new SubtitleComponent(subtitle)));
	}

	@Override
	public void toggleSubtitles(boolean enabled) {
		ServerFeature service = LabyMod.references().serverFeatureService().get();
		for (Subtitle subtitle : subtitles) {
			if (enabled)
				service.getOrCreateUserFeature(subtitle.getUniqueId()).setSubtitle(new SubtitleComponent(subtitle));
			else
				service.getOrCreateUserFeature(subtitle.getUniqueId()).setSubtitle(null);
		}
	}

	/**
	 * Injects badges into {@link GroupTextTag} by overriding the rendered snapshot.
	 */
	@ExclusiveTo(LABY_4)
	@Mixin(value = GameUserSnapshotFactory.class, remap = false)
	private static abstract class MixinGameUserSnapshotFactory {

		@Shadow
		@Final
		private LabyAPI labyAPI;

		@Inject(method = "create(Lnet/labymod/api/user/GameUser;Lnet/labymod/api/laby3d/renderer/snapshot/Extras;)Lnet/labymod/api/client/render/state/entity/GameUserSnapshot;", at = @At("HEAD"), cancellable = true)
		private void injectCreate(GameUser user, Extras extras, CallbackInfoReturnable<GameUserSnapshot> cir) {
			cir.setReturnValue(new GrieferUtilsGameUserSnapshot(user, extras, labyAPI));
		}

	}

	/**
	 * Render player percentage in tablist.
	 */
	@ExclusiveTo(LABY_4)
	@Mixin(value = PlayerListRenderer.class, remap = false)
	private static class MixinPlayerListRenderer {

		@Inject(method = "render", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
		private void injectRender(ScreenContext context, LabyAPI labyAPI, Bounds bounds, boolean update, CallbackInfo ci, int screenWidth, float columnsWidth, float backgroundWidth, RenderableComponent headerRenderableComponent, RenderableComponent footerRenderableComponent, int x, int y) {
			Tags.renderUserPercentage(x + backgroundWidth);
		}

	}

}
