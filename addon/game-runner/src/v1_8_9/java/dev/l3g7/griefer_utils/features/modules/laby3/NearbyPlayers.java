/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.modules.laby3;

import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.modules.Laby3Module;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.util.PlayerUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

@Singleton
public class NearbyPlayers extends Laby3Module {

	private static List<EntityOtherPlayerMP> players = new ArrayList<>();

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Spieler in der Nähe")
		.description("Zeigt Spieler in deiner Nähe an.")
		.icon("radar");

	@Override
	public String[] getValues() {
		if (world() == null || player() == null)
			return getDefaultValues();

		players = world().getEntities(EntityOtherPlayerMP.class, p -> !PlayerUtil.isNPC(p) && p.getDistanceToEntity(player()) < 1000);
		players.sort(Comparator.comparingDouble(e -> e.getDistanceToEntity(player())));
		return new String[] {String.valueOf(players.size())};
	}

	@Override
	public int getLines() {
		return players.size() + 1;
	}

	@Override
	public String[] getDefaultValues() {
		players.clear();
		return new String[] {"0"};
	}

	@Override
	public void draw(double x, double y, double rightX) {
		super.draw(x, y, rightX);

		float fX = (float) (rightX == -1 ? x : rightX);
		float fY = (float) y;

		for (EntityOtherPlayerMP player : players) {
			if (rightX == -1)
				drawPlayer(player, fX, fY += 10);
			else
				drawPlayerRight(player, fX, fY += 10);
		}
	}

	private void drawPlayerRight(EntityOtherPlayerMP player, float x, float y) {
		int distance = (int) player.getDistanceToEntity(player());

		x -= mc.fontRendererObj.getStringWidth(distance + "m");
		mc.fontRendererObj.drawStringWithShadow(distance + "m", x, y, Integer.MAX_VALUE);
		if (distance < 10)
			x -= mc.fontRendererObj.getCharWidth('0');

		x -= mc.fontRendererObj.getStringWidth(player.getDisplayName().getFormattedText() + " ");
		mc.fontRendererObj.drawStringWithShadow(player.getDisplayName().getFormattedText(), x, y, Integer.MAX_VALUE);
		x -= 10;

		DrawUtils.bindTexture(player.getLocationSkin());
		DrawUtils.drawTexture(x, y, 32, 32, 32, 32, 8, 8); // First layer
		DrawUtils.drawTexture(x, y, 160, 32, 32, 32, 8, 8); // Second layer
	}

	private void drawPlayer(EntityOtherPlayerMP player, float x, float y) {
		int distance = (int) player.getDistanceToEntity(player());
		if (distance < 10)
			x += mc.fontRendererObj.getCharWidth('0');

		Text text = toText(distance + "m");
		mc.fontRendererObj.drawStringWithShadow(text.getText(), x, y, text.getColor());
		x += mc.fontRendererObj.getStringWidth(text.getText()) + 2;

		DrawUtils.bindTexture(player.getLocationSkin());
		DrawUtils.drawTexture(x, y, 32, 32, 32, 32, 8, 8); // First layer
		DrawUtils.drawTexture(x, y, 160, 32, 32, 32, 8, 8); // Second layer

		// Use display name from tab list for applied text mods
		IChatComponent displayName = mc.getNetHandler().getPlayerInfo(player.getUniqueID()).getDisplayName();
		if (displayName != null)
			mc.fontRendererObj.drawStringWithShadow(displayName.getFormattedText(), x + 10, y, Integer.MAX_VALUE);
	}

}
