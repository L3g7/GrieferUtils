/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * This file has been modified by itzW0lf.
 */

package dev.l3g7.griefer_utils.features.widgets.other;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.settings.types.SliderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.PlayerUtil;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.widgets.Laby3Widget;
import dev.l3g7.griefer_utils.features.widgets.Laby4Widget;
import dev.l3g7.griefer_utils.features.widgets.Widget;
import dev.l3g7.griefer_utils.labymod.laby4.settings.Icons;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.Style;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.gfx.pipeline.renderer.text.TextRenderingOptions;
import net.labymod.api.client.gui.hud.position.HudSize;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.gui.screen.state.ScreenCanvas;
import net.labymod.api.client.render.font.RenderableComponent;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

@Singleton
public class InvisibilityWarning extends Widget {

	private final SliderSetting range = SliderSetting.create()
		.name("Warnbereich (Blöcke)")
		.description("Maximale Entfernung, ab der unsichtbare Spieler angezeigt und gemeldet werden.")
		.icon("eye_black")
		.min(1).max(100)
		.defaultValue(20);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Unsichtbare Spieler")
		.description("Zeigt unsichtbare Spieler in deiner Nähe an und warnt per Benachrichtigung.")
		.icon("invisibility")
		.since("2.5.0")
		.subSettings(range);

	private final List<EntityOtherPlayerMP> invisiblePlayers = new ArrayList<>();
	private final Set<UUID> warnedPlayers = new HashSet<>();
	private int totalCount = 0;

	private void updatePlayers() {
		invisiblePlayers.clear();
		if (world() == null || player() == null)
			return;

		invisiblePlayers.addAll(world().getEntities(EntityOtherPlayerMP.class,
			p -> !PlayerUtil.isNPC(p) && p.isInvisible() && p.getDistanceToEntity(player()) <= range.get()));
		totalCount = invisiblePlayers.size();
		invisiblePlayers.sort(Comparator.comparingDouble(e -> e.getDistanceToEntity(player())));

		Set<UUID> currentUUIDs = new HashSet<>();
		for (EntityOtherPlayerMP p : invisiblePlayers) {
			currentUUIDs.add(p.getUniqueID());
			if (!warnedPlayers.contains(p.getUniqueID())) {
				warnedPlayers.add(p.getUniqueID());
				LabyBridge.labyBridge.notify("§4§lUnsichtbarer Spieler!", "§c" + p.getName() + " §fist in deiner Nähe unsichtbar!");
			}
		}
		warnedPlayers.retainAll(currentUUIDs);
	}

	@Override
	protected LabyWidget getLaby3() {
		return new InvisibilityWarningL3();
	}

	@Override
	protected LabyWidget getLaby4() {
		return new InvisibilityWarningL4();
	}

	@ExclusiveTo(LABY_3)
	private class InvisibilityWarningL3 extends Laby3Widget {

		@MainElement
		private final SwitchSetting enabled = SwitchSetting.create()
			.name("Unsichtbare Spieler")
			.description("Zeigt unsichtbare Spieler in deiner Nähe an und warnt per Benachrichtigung.")
			.icon("invisibility")
			.subSettings(range);

		@Override
		public String[] getValues() {
			if (world() == null || player() == null)
				return getDefaultValues();
			updatePlayers();
			return new String[]{String.valueOf(totalCount)};
		}

		@Override
		public int getLines() {
			return invisiblePlayers.size() + 1;
		}

		@Override
		public String[] getDefaultValues() {
			invisiblePlayers.clear();
			return new String[]{"0"};
		}

		@Override
		public void draw(double x, double y, double rightX) {
			super.draw(x, y, rightX);

			float fX = (float) (rightX == -1 ? x : rightX);
			float fY = (float) y;

			for (EntityOtherPlayerMP player : invisiblePlayers) {
				float lineX = fX;
				fY += 10;
				int distance = (int) player.getDistanceToEntity(player());

				if (rightX == -1) {
					if (distance < 10)
						lineX += mc.fontRendererObj.getCharWidth('0');

					Text text = toText(distance + "m");
					mc.fontRendererObj.drawStringWithShadow(text.getText(), lineX, fY, text.getColor());
					lineX += mc.fontRendererObj.getStringWidth(text.getText()) + 2;

					DrawUtils.bindTexture(player.getLocationSkin());
					DrawUtils.drawTexture(lineX, fY, 32, 32, 32, 32, 8, 8);
					DrawUtils.drawTexture(lineX, fY, 160, 32, 32, 32, 8, 8);

					IChatComponent displayName = mc.getNetHandler().getPlayerInfo(player.getUniqueID()).getDisplayName();
					if (displayName != null)
						mc.fontRendererObj.drawStringWithShadow(displayName.getFormattedText(), lineX + 10, fY, Integer.MAX_VALUE);
				} else {
					lineX -= mc.fontRendererObj.getStringWidth(distance + "m");
					mc.fontRendererObj.drawStringWithShadow(distance + "m", lineX, fY, Integer.MAX_VALUE);
					if (distance < 10)
						lineX -= mc.fontRendererObj.getCharWidth('0');

					lineX -= mc.fontRendererObj.getStringWidth(player.getDisplayName().getFormattedText() + " ");
					mc.fontRendererObj.drawStringWithShadow(player.getDisplayName().getFormattedText(), lineX, fY, Integer.MAX_VALUE);
					lineX -= 10;

					DrawUtils.bindTexture(player.getLocationSkin());
					DrawUtils.drawTexture(lineX, fY, 32, 32, 32, 32, 8, 8);
					DrawUtils.drawTexture(lineX, fY, 160, 32, 32, 32, 8, 8);
				}
			}
		}
	}

	@ExclusiveTo(LABY_4)
	private class InvisibilityWarningL4 extends Laby4Widget {

		private float maxDistWidth = 0;

		@Override
		protected void createText() {}

		@Override
		public void onTick(boolean isEditorContext) {
			updatePlayers();

			lines.clear();
			maxDistWidth = 0;
			createLine("Unsichtbare Spieler", String.valueOf(totalCount));

			for (EntityOtherPlayerMP player : invisiblePlayers)
				lines.add(new InvisiblePlayerLine(player));
		}

		@ExclusiveTo(LABY_4)
		private class InvisiblePlayerLine extends CustomRenderTextLine {

			RenderableComponent distance, player;

			public InvisiblePlayerLine(EntityOtherPlayerMP player) {
				super(InvisibilityWarningL4.this);
				int distance = (int) player.getDistanceToEntity(player());

				NetworkPlayerInfo playerInfo = mc().getNetHandler().getPlayerInfo(player.getUniqueID());
				if (playerInfo == null)
					return;

				IChatComponent displayName = playerInfo.getDisplayName();
				if (displayName == null)
					return;

				this.distance = createRenderableComponent(Component.text(distance + "m "));
				this.player = createRenderableComponent(
					Component.icon(Icons.offset(Icon.head(player.getUniqueID()), 0, -1), Style.builder().color(TextColor.color(-1)).build(), mc().fontRendererObj.FONT_HEIGHT)
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
			public void renderLine(ScreenContext context, float x, float y, float space, HudSize hudWidgetSize) {
				ScreenCanvas renderState = context.canvas();

				int flags = TextRenderingOptions.SHADOW;
				if (this.floatingPointPosition)
					flags |= TextRenderingOptions.USE_FLOATING_POINT_VALUES;

				renderState.submitRenderableComponent(distance, x + maxDistWidth - distance.getWidth(), y, -1, flags);
				renderState.submitRenderableComponent(player, x + maxDistWidth, y, -1, flags);
			}
		}
	}
}
