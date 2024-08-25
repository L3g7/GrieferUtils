/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.misc;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.PlayerUtil;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.widgets.Laby3Widget;
import dev.l3g7.griefer_utils.features.widgets.Laby4Widget;
import dev.l3g7.griefer_utils.features.widgets.LabyWidget;
import dev.l3g7.griefer_utils.labymod.laby4.settings.OffsetIcon;
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

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

@Singleton
public class NearbyPlayers extends LabyWidget {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
			.name("Spieler in der Nähe")
			.description("Zeigt Spieler in deiner Nähe an.")
			.icon("radar");

	private final List<EntityOtherPlayerMP> players = new ArrayList<>();

	private void updatePlayers() {
		players.clear();
		if (world() != null) {
			players.addAll(world().getEntities(EntityOtherPlayerMP.class, p -> !PlayerUtil.isNPC(p) && p.getDistanceToEntity(player()) < 1000));
			players.sort(Comparator.comparingDouble(e -> e.getDistanceToEntity(player())));
		}
	}

	@Override
	protected Object getLaby3() {
		return new NearbyPlayersL3();
	}

	@Override
	protected Object getLaby4() {
		return new NearbyPlayersL4();
	}

	@ExclusiveTo(LABY_3)
	private class NearbyPlayersL3 extends Laby3Widget {

		@MainElement
		private final SwitchSetting enabled = SwitchSetting.create()
				.name("Spieler in der Nähe")
				.description("Zeigt Spieler in deiner Nähe an.")
				.icon("radar");

		@Override
		public String[] getValues() {
			if (world() == null || player() == null)
				return getDefaultValues();

			updatePlayers();
			return new String[]{String.valueOf(players.size())};
		}

		@Override
		public int getLines() {
			return players.size() + 1;
		}

		@Override
		public String[] getDefaultValues() {
			players.clear();
			return new String[]{"0"};
		}

		@Override
		public void draw(double x, double y, double rightX) {
			super.draw(x, y, rightX);

			float fX = (float) (rightX == -1 ? x : rightX);
			float fY = (float) y;

			for (EntityOtherPlayerMP player : players) {
				fY += 10;
				int distance = (int) player.getDistanceToEntity(player());
				if (rightX == -1) { // Aligned left
					if (distance < 10)
						fX += mc.fontRendererObj.getCharWidth('0');

					Text text = toText(distance + "m");
					mc.fontRendererObj.drawStringWithShadow(text.getText(), fX, fY, text.getColor());
					fX += mc.fontRendererObj.getStringWidth(text.getText()) + 2;

					DrawUtils.bindTexture(player.getLocationSkin());
					DrawUtils.drawTexture(fX, fY, 32, 32, 32, 32, 8, 8); // First layer
					DrawUtils.drawTexture(fX, fY, 160, 32, 32, 32, 8, 8); // Second layer

					// Use display name from tab list for applied text mods
					IChatComponent displayName = mc.getNetHandler().getPlayerInfo(player.getUniqueID()).getDisplayName();
					if (displayName != null)
						mc.fontRendererObj.drawStringWithShadow(displayName.getFormattedText(), fX + 10, fY, Integer.MAX_VALUE);
				} else { // Aligned right
					fX -= mc.fontRendererObj.getStringWidth(distance + "m");
					mc.fontRendererObj.drawStringWithShadow(distance + "m", fX, fY, Integer.MAX_VALUE);
					if (distance < 10)
						fX -= mc.fontRendererObj.getCharWidth('0');

					fX -= mc.fontRendererObj.getStringWidth(player.getDisplayName().getFormattedText() + " ");
					mc.fontRendererObj.drawStringWithShadow(player.getDisplayName().getFormattedText(), fX, fY, Integer.MAX_VALUE);
					fX -= 10;

					DrawUtils.bindTexture(player.getLocationSkin());
					DrawUtils.drawTexture(fX, fY, 32, 32, 32, 32, 8, 8); // First layer
					DrawUtils.drawTexture(fX, fY, 160, 32, 32, 32, 8, 8); // Second layer
				}
			}
		}

	}

	@ExclusiveTo(LABY_4)
	private class NearbyPlayersL4 extends Laby4Widget {

		private float maxDistWidth = 0;

		@Override
		protected void createText() {}

		@Override
		public void onTick(boolean isEditorContext) {
			updatePlayers();

			lines.clear();
			maxDistWidth = 0;
			createLine("Spieler in der Nähe", String.valueOf(players.size()));

			for (EntityOtherPlayerMP player : players)
				lines.add(new NearbyPlayerLine(player));
		}

		@ExclusiveTo(LABY_4)
		private class NearbyPlayerLine extends CustomRenderTextLine {

			RenderableComponent distance, player;

			public NearbyPlayerLine(EntityOtherPlayerMP player) {
				super(NearbyPlayersL4.this);
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
				return maxDistWidth + (player == null ? 0 : player.getWidth());
			}

			@Override
			public void renderLine(Stack stack, float x, float y, float space, HudSize hudWidgetSize) {
				BUILDER.pos(x + maxDistWidth - distance.getWidth(), y).shadow(true).useFloatingPointPosition(this.floatingPointPosition).text(distance).render(stack);
				BUILDER.pos(x + maxDistWidth, y).shadow(true).useFloatingPointPosition(this.floatingPointPosition).text(player).render(stack);
			}

		}

	}

}
