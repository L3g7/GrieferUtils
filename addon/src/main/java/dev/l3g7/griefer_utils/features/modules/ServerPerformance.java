/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.features.modules;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.network.PacketEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.misc.Named;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S05PacketSpawnPosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

/**
 * Concept by <a href="https://github.com/Pleezon/ServerTPS/blob/bebc5e75e592fdc1bf9401b1a5d42454028227b0/src/main/java/de/techgamez/pleezon/Main.java">Pleezon/ServerTPS</a>
 */
@Singleton
public class ServerPerformance extends Module {

	private Double currentTPS = null;
	private Long lastWorldTime = null;
	private final List<Double> tps = new ArrayList<>();
	private long lastMillis = 0;
	private int lastTripTime = 0;

	private final DropDownSetting<DisplayMode> displayMode = new DropDownSetting<>(DisplayMode.class)
		.name("Anzeigemodus")
		.icon("wooden_board")
		.description("Ob die Performance in Prozent angezeigt oder in TPS angezeigt werden soll.")
		.defaultValue(DisplayMode.PERCENT);

	private final BooleanSetting applyColor = new BooleanSetting()
		.name("Anzeige färben")
		.icon("labymod:settings/settings/tabping_colored")
		.description("Ob die Performance eingefärbt werden soll.")
		.defaultValue(true);

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Server-\nPerformance")
		.description("Zeigt eine (relativ genaue) Schätzung der aktuellen Server-Performance an.")
		.icon("measurement_circle_thingy")
		.subSettings(displayMode, applyColor);

	@Override
	public String[] getDefaultValues() {
		return new String[]{"?"};
	}

	@Override
	public String[] getValues() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<List<Text>> getTextValues() {
		if (currentTPS == null)
			return getDefaultTextValues();

		// calculate color
		int r, g;
		if (currentTPS < 15) {
			r = 255;
			g = Math.max((int) (6.8 * (5d * currentTPS - 62.5) + 170), 0);
		} else {
			r = (int) (255 - 6.8 * (5d * currentTPS - 75));
			g = 255;
		}

		// create text representation
		String displayTPS = displayMode.get() == DisplayMode.PERCENT ? Math.round(currentTPS / .002) / 100 + "%" : String.valueOf(currentTPS);

		return Collections.singletonList(Collections.singletonList(applyColor.get() ? Text.getText(displayTPS, r, g, 0x55) : Text.getText(displayTPS)));
	}

	@EventListener
	public void onPacket(PacketEvent.PacketReceiveEvent<Packet<?>> event) {
		Packet<?> packet = event.packet;

		if (packet instanceof S03PacketTimeUpdate) {
			calcTps(((S03PacketTimeUpdate) packet));
		} else if (packet instanceof S05PacketSpawnPosition) {
			lastWorldTime = null;
			tps.clear();
		}
	}

	private void calcTps(S03PacketTimeUpdate packet) {
		if (player() == null || mc.getNetHandler() == null)
			return;

		// Time it takes for the packet to reach the client
		NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(mc.getSession().getProfile().getId());
		if (playerInfo == null)
			return;

		int tripTime = playerInfo.getResponseTime() / 2;

		long currentWorldTime = packet.getTotalWorldTime();
		if (currentWorldTime < 0) // the doDayLightCycle gamerule is disabled
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