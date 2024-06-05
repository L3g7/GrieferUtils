/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.modules.laby4;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.labymod.laby4.settings.OffsetIcon;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.modules.Laby4Module;
import dev.l3g7.griefer_utils.core.util.PlayerUtil;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.gui.hud.position.HudSize;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.render.font.RenderableComponent;
import net.labymod.api.client.render.matrix.Stack;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

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

	public class NearbyPlayerLine extends CustomRenderTextLine {

		RenderableComponent distance, player;

		public NearbyPlayerLine(EntityOtherPlayerMP player) {
			super(NearbyPlayers.this);
			int distance = (int) player.getDistanceToEntity(player());

			// Use display name from tab list for applied text mods
			NetworkPlayerInfo playerInfo = mc().getNetHandler().getPlayerInfo(player.getUniqueID());
			if (playerInfo == null)
				return;

			IChatComponent displayName = playerInfo.getDisplayName();
			if (displayName == null)
				return;

			this.distance = createRenderableComponent(Component.text(distance + "m "));
			this.player = createRenderableComponent(
				Component.icon(new OffsetIcon(Icon.head(player.getUniqueID()), 0, -1), Style.builder().color(TextColor.color(-1)).build(), mc().fontRendererObj.FONT_HEIGHT)
					.append(Component.text(" ")).append(c(displayName)));

			maxDistWidth = Math.max(maxDistWidth, this.distance.getWidth());
		}

		@Override
		public boolean isAvailable() {
			return distance != null && player != null;
		}

		@Override
		public float getWidth() {
			return maxDistWidth + player.getWidth();
		}

		@Override
		public void renderLine(Stack stack, float x, float y, float space, HudSize hudWidgetSize) {
			BUILDER.pos(x + maxDistWidth - distance.getWidth(), y).shadow(true).useFloatingPointPosition(this.floatingPointPosition).text(distance).render(stack);
			BUILDER.pos(x + maxDistWidth, y).shadow(true).useFloatingPointPosition(this.floatingPointPosition).text(player).render(stack);
		}

	}

}
