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
