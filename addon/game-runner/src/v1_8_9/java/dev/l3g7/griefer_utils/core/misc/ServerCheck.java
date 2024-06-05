/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc;

import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.GrieferGamesJoinEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.ServerQuitEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.ServerSwitchEvent;

import static dev.l3g7.griefer_utils.core.api.event.event_bus.Priority.HIGHEST;
import static dev.l3g7.griefer_utils.core.api.event.event_bus.Priority.LOWEST;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.world;

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
