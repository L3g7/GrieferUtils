/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.misc;

import dev.l3g7.griefer_utils.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.v1_8_9.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent.ServerQuitEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent.ServerSwitchEvent;

import static dev.l3g7.griefer_utils.api.event.event_bus.Priority.HIGHEST;
import static dev.l3g7.griefer_utils.api.event.event_bus.Priority.LOWEST;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.world;

@Singleton
public class ServerCheck {

	private static boolean onGrieferGames;
	private static boolean onCitybuild;

	public static boolean isOnGrieferGames() {
		return onGrieferGames || (!LabyBridge.labyBridge.obfuscated() && world() != null);
	}

	public static boolean isOnCitybuild() {
		return onCitybuild || (!LabyBridge.labyBridge.obfuscated() && world() != null);
	}

	@EventListener(priority = HIGHEST)
	public void onPacketReceive(GrieferGamesJoinEvent event) {
		onGrieferGames = true;
	}

	@EventListener
	public void onCitybuildJoin(CitybuildJoinEvent event) {
		onCitybuild = true;
	}

	@EventListener
	public void onServerSwitch(ServerSwitchEvent event) {
		onCitybuild = false;
	}

	@EventListener(priority = LOWEST)
	public void onServerQuit(ServerQuitEvent event) {
		onGrieferGames = false;
	}

}
