/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.modules;

import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.laby4.settings.OffsetIcon;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.features.Laby4Module;
import dev.l3g7.griefer_utils.v1_8_9.util.PlayerUtil;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.hud.position.HudSize;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.render.font.RenderableComponent;
import net.labymod.api.client.render.matrix.Stack;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.l3g7.griefer_utils.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.*;

@Singleton
public class NearbyPlayers extends Laby4Module {

	private float maxDistWidth = 0;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Spieler in der Nähe")
		.description("Zeigt Spieler in deiner Nähe an.")
		.icon("radar");

	@Override
	protected void createText() {}

	@Override
	public void onTick(boolean isEditorContext) {
		List<EntityOtherPlayerMP> players = new ArrayList<>();
		if (world() != null)
			players = world().getEntities(EntityOtherPlayerMP.class, p -> !PlayerUtil.isNPC(p) && p.getDistanceToEntity(player()) < 1000);

		players.sort(Comparator.comparingDouble(e -> e.getDistanceToEntity(player())));

		lines.clear();
		maxDistWidth = 0;
		createLine("Spieler in der Nähe", String.valueOf(players.size()));

		for (EntityOtherPlayerMP player : players)
			lines.add(new NearbyPlayerLine(player));
	}

	public class NearbyPlayerLine extends TextLine {

		RenderableComponent distance, player;

		public NearbyPlayerLine(EntityOtherPlayerMP player) {
			super(NearbyPlayers.this, (Component) null, "");

			int distance = (int) player.getDistanceToEntity(player());

			// Use display name from tab list for applied text mods
			IChatComponent displayName = mc().getNetHandler().getPlayerInfo(player.getUniqueID()).getDisplayName();
			if (displayName == null)
				return;

			this.distance = createRenderableComponent(Component.text(distance + "m "));
			this.player = createRenderableComponent(
				Component.icon(new OffsetIcon(Icon.head(player.getUniqueID()), 0, -1), Style.builder().color(TextColor.color(-1)).build(), mc().fontRendererObj.FONT_HEIGHT)
					.append(Component.text(" ")).append(c(displayName)));

			this.renderableComponent = this.distance;
			maxDistWidth = Math.max(maxDistWidth, this.distance.getWidth());
		}

		@Override
		protected void flushInternal() {}

		private RenderableComponent createRenderableComponent(Component c) {
			return RenderableComponent.builder().disableCache().format(c);
		}

		@Override
		public void renderLine(Stack stack, float x, float y, float space, HudSize hudWidgetSize) {
			BUILDER.pos(x + maxDistWidth - distance.getWidth(), y).shadow(true).useFloatingPointPosition(this.floatingPointPosition).text(distance).render(stack);
			BUILDER.pos(x + maxDistWidth, y).shadow(true).useFloatingPointPosition(this.floatingPointPosition).text(player).render(stack);
		}

	}

}
