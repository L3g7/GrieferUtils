/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.tags.laby4;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.core.misc.tags.Tags;
import dev.l3g7.griefer_utils.features.uncategorized.settings.Badges;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.entity.player.tag.PositionType;
import net.labymod.api.client.entity.player.tag.tags.NameTag;
import net.labymod.api.client.render.font.RenderableComponent;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

@Singleton
@ExclusiveTo(LABY_4)
public class GrieferUtilsTextTag extends NameTag {

	@OnEnable
	private void onEnable() {
		Laby.labyAPI().tagRegistry().register("grieferutils_text", PositionType.ABOVE_NAME, this);
	}

	@Override
	protected RenderableComponent getRenderableComponent() {
		if (!Badges.showBadges())
			return null;

		var badge = Tags.getBadge(entity.getUniqueId());
		if (badge.isEmpty())
			return null;

		return RenderableComponent.of(Component.text("GrieferUtils ")
			.color(TextColor.color(0xFFFFFF))
			.append(Component.text(badge.get().title())));
	}

	@Override
	public float getScale() {
		return 0.5F;
	}

}
