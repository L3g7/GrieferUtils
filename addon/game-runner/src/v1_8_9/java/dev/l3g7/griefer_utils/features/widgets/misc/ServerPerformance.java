/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.misc;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.events.network.PacketEvent.PacketReceiveEvent;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.widgets.SimpleWidget;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S05PacketSpawnPosition;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

/**
 * Concept by <a href="https://github.com/Pleezon/ServerTPS/blob/bebc5e75e592fdc1bf9401b1a5d42454028227b0/src/main/java/de/techgamez/pleezon/Main.java">Pleezon/ServerTPS</a>
 */
@Singleton
public class ServerPerformance extends SimpleWidget {

	private Double currentTPS = null;
	private Long lastWorldTime = null;
	private final List<Double> tps = new ArrayList<>();
	private long lastMillis = 0;
	private int lastTripTime = 0;

	private final DropDownSetting<DisplayMode> displayMode = DropDownSetting.create(DisplayMode.class)
		.name("Anzeigemodus")
		.icon("wooden_board")
		.description("Ob die Performance in Prozent angezeigt oder in TPS angezeigt werden soll.")
		.defaultValue(DisplayMode.PERCENT);

	private final SwitchSetting applyColor = SwitchSetting.create()
		.name("Anzeige färben")
		.icon("labymod_3/tabping_colored")
		.description("Ob die Performance eingefärbt werden soll.")
		.defaultValue(true);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Server-\nPerformance")
		.description("Zeigt eine (relativ genaue) Schätzung der aktuellen Server-Performance an.")
		.icon("measurement_circle_thingy")
		.subSettings(displayMode, applyColor);

	@Override
	public String getValue() {
		if (currentTPS == null)
			return "?";

		if (displayMode.get() == DisplayMode.PERCENT)
			return Math.round(currentTPS / .002) / 100 + "%";
		else
			return String.valueOf(currentTPS);
	}

	@Override
	public int getColor() {
		if (!applyColor.get() || currentTPS == null)
			return -1;

		int r, g, b;
		if (currentTPS < 15) {
			r = 0xFF;
			g = (int) (0xFF * (currentTPS / 15d));
			b = 0;
		} else {
			r = (int) (0x55 + 0xAA * (20 - currentTPS) / 5d);
			g = 255;
			b = (int) (0x55 * (currentTPS - 15) / 5d);
		}

		return DrawUtils.toRGB(r, g, b);
	}

	@EventListener(triggerWhenDisabled = true)
	public void onSpawn(PacketReceiveEvent<S05PacketSpawnPosition> event) {
		// Reset tps
		lastWorldTime = null;
		tps.clear();
	}

	@EventListener(triggerWhenDisabled = true)
	public void onTimeUpdate(PacketReceiveEvent<S03PacketTimeUpdate> event) {
		if (player() == null || mc().getNetHandler() == null)
			return;

		// Calculate time it takes for the packet to reach the client
		NetworkPlayerInfo playerInfo = mc().getNetHandler().getPlayerInfo(mc().getSession().getProfile().getId());
		if (playerInfo == null)
			return;

		int tripTime = playerInfo.getResponseTime() / 2;

		long currentWorldTime = event.packet.getTotalWorldTime();
		if (currentWorldTime < 0)
			// the doDayLightCycle game rule is disabled
			return;

		long currentMillis = System.currentTimeMillis();

		if (lastWorldTime == null) {
			lastWorldTime = currentWorldTime;
			lastMillis = currentMillis;
			lastTripTime = tripTime;
			return;
		}

		int tripTimeDiff = (tripTime - lastTripTime) / 2;
		long ageDiff = currentWorldTime - lastWorldTime;
		long timeDiff = currentMillis - (lastMillis + tripTimeDiff);
		double currentTps = ageDiff / (timeDiff / 1000d);

		if (currentTps < 0)
			return;

		tps.add(Math.min(currentTps, 20));

		double averageTps = tps.stream().reduce(Double::sum).orElse(0d) / tps.size();
		currentTPS = Math.round(averageTps * 100) / 100d; // Round to two decimals

		if (tps.size() > 25)
			tps.remove(0);

		lastWorldTime = currentWorldTime;
		lastTripTime = tripTime;
		lastMillis = currentMillis;
	}

	private enum DisplayMode implements Named {
		PERCENT("Prozent"),
		TPS("TPS");

		private final String name;

		DisplayMode(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

	}

}
