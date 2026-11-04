/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.badges.laby4;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.badges.Badges;
import dev.l3g7.griefer_utils.core.misc.badges.Badges.BadgeManager;
import net.labymod.api.LabyAPI;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.client.render.font.RenderableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

@Bridge
@Singleton
@ExclusiveTo(LABY_4)
public class Laby4BadgeManager implements BadgeManager {

	@Override
	public void setOnline(UUID uuid) { /* NO-OP */ }

	@Override
	public void setOffline(UUID uuid) { /* NO-OP */ }

	@Override
	public void toggleBadges(boolean enabled) { /* NO-OP */ }

	/**
	 * Render player percentage in tablist.
	 */
	@Pseudo
	@ExclusiveTo(LABY_4)
	@Mixin(targets = "net.labymod.core.client.gui.screen.activity.activities.ingame.playerlist.PlayerListRenderer", remap = false)
	private static class MixinPlayerListRenderer {

		@Inject(method = "render", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
		private void injectRender(ScreenContext context, LabyAPI labyAPI, Bounds bounds, boolean update, @Coerce Object strip, CallbackInfo ci, float screenWidth, float screenHeight, RenderableComponent header, RenderableComponent footer, Icon banner, boolean showBanner, float guiScale, float scale, float stripHeight, float stripSpace, int top, float fixedHeight, int frameWidth, float columnsWidth, float backgroundWidth, int rowsPerColumn, float listHeight, float contentHeight, float originY, float slide, float hitOriginX, float hitOriginY, float alpha, boolean translucent, int x, int y, int statsY) {
			Badges.renderUserPercentage(x + backgroundWidth);
		}

	}

}
