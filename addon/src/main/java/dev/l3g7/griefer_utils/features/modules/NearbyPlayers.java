/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.modules;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.PlayerUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

@Singleton
public class NearbyPlayers extends Module {

	private static List<EntityOtherPlayerMP> players = new ArrayList<>();

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Spieler in der Nähe")
		.description("Zeigt Spieler in deiner Nähe an.")
		.icon("radar");

	@Override
	public String[] getValues() {
		players = world().getEntities(EntityOtherPlayerMP.class, p -> !PlayerUtil.isNPC(p));
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

	private static void drawPlayerRight(EntityOtherPlayerMP player, float x, float y) {
		int distance = (int) player.getDistanceToEntity(player());

		x -= mc.fontRendererObj.getStringWidth(distance + "m");
		mc.fontRendererObj.drawStringWithShadow(distance + "m", x, y, Integer.MAX_VALUE);
		if (distance < 10)
			x -= mc.fontRendererObj.getCharWidth('0');

		x -= mc.fontRendererObj.getStringWidth(player.getDisplayName().getFormattedText() + " ");
		mc.fontRendererObj.drawStringWithShadow(player.getDisplayName().getFormattedText(), x, y, Integer.MAX_VALUE);
		x -= 10;

		drawUtils().bindTexture(player.getLocationSkin());
		drawUtils().drawTexture(x, y, 32, 32, 32, 32, 8, 8); // First layer
		drawUtils().drawTexture(x, y, 160, 32, 32, 32, 8, 8); // Second layer
	}

	private static void drawPlayer(EntityOtherPlayerMP player, float x, float y) {
		int distance = (int) player.getDistanceToEntity(player());
		if (distance < 10)
			x += mc.fontRendererObj.getCharWidth('0');

		mc.fontRendererObj.drawStringWithShadow(distance + "m", x, y, Integer.MAX_VALUE);
		x += mc.fontRendererObj.getStringWidth(distance + "m") + 2;

		drawUtils().bindTexture(player.getLocationSkin());
		drawUtils().drawTexture(x, y, 32, 32, 32, 32, 8, 8); // First layer
		drawUtils().drawTexture(x, y, 160, 32, 32, 32, 8, 8); // Second layer

		mc.fontRendererObj.drawStringWithShadow(player.getDisplayName().getFormattedText(), x + 10, y, Integer.MAX_VALUE);
	}

}
