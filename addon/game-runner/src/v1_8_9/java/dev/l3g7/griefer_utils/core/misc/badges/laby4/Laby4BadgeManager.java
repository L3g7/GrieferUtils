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
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.gfx.pipeline.renderer.text.TextRenderingOptions;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.gui.screen.state.ScreenCanvas;
import net.labymod.api.client.render.font.FontSize;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.util.math.MathHelper;

import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.features.uncategorized.settings.Badges.showBadges;
import static dev.l3g7.griefer_utils.features.uncategorized.settings.Badges.showPercentage;

@Bridge
@Singleton
@ExclusiveTo(LABY_4)
public class Laby4BadgeManager implements BadgeManager {

    private static final ResourceLocation LABY_BADGE = ResourceLocation.labymod("textures/labymod_badge.svg");

	@Override
	public void setOnline(UUID uuid) { /* NO-OP */ }

	@Override
	public void setOffline(UUID uuid) { /* NO-OP */ }

	@Override
	public void toggleBadges(boolean enabled) { /* NO-OP */ }

	public static void renderStats(ScreenContext context, float backgroundWidth, int x, int y, int labyOnlineCount, int labyUserCount) {
		if (!showBadges() || !showPercentage.get())
			return;

		String text = Badges.getUserCount();
		if (text == null)
			return;

		ScreenCanvas canvas = context.canvas();
		float fontSize = FontSize.PredefinedFontSize.SMALL.fontSize().getSize();
		int iconSize = (int) ((float) 12 * fontSize);

		// Render GrieferUtils stats
		{
			var icon = Icon.texture(Laby.references().resourceLocationFactory().create("griefer_utils", "icons/high_res/" + Badges.icon + ".png"));
			Component component = Component.text(text);

			canvas.submitComponent(component,
				x + backgroundWidth + 2 - canvas.getTextWidth(component) * fontSize,
				y, 0xFFFFFFFF, fontSize, TextRenderingOptions.SHADOW);

			canvas.submitComponent(Component.icon(icon, iconSize),
				(float) x + backgroundWidth + 2F - canvas.getTextWidth(component) * fontSize - iconSize,
				y, 0xFFFFFFFF, fontSize, TextRenderingOptions.NONE);
		}

		// Render LabyMod badge to original stats
		Component component = Component.text(labyUserCount, NamedTextColor.GRAY)
			.append(Component.text("/", NamedTextColor.DARK_GRAY))
			.append(Component.text(labyOnlineCount, NamedTextColor.GRAY))
			.append(Component.space())
			.append(Component.text(MathHelper.ceil(100F / (float) labyOnlineCount * (float) labyUserCount) + "%", NamedTextColor.GREEN));

		var icon = Icon.svg(LABY_BADGE);

        canvas.submitComponent(Component.icon(icon, iconSize),
	        (float)x + backgroundWidth + 2F - canvas.getTextWidth(component) * fontSize - iconSize,
	        (float)y - canvas.getLineHeight() * fontSize, 0xFFFFFFFF, fontSize, TextRenderingOptions.NONE);
	}

}
