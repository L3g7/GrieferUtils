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

package dev.l3g7.griefer_utils.misc;

import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.griefergames.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent;
import net.labymod.core.asm.LabyModCoreMod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.world;

@Singleton
public class ServerCheck {

	private static boolean onGrieferGames;
	private static boolean onCitybuild;

	public static boolean isOnGrieferGames() {
		return onGrieferGames || (!LabyModCoreMod.isObfuscated() && world() != null);
	}

	public static boolean isOnCitybuild() {
		return onCitybuild || (!LabyModCoreMod.isObfuscated() && world() != null);
	}

	@EventListener(priority = EventPriority.HIGHEST)
	public void onServerJoin(ServerEvent.ServerJoinEvent event) {
		String server = event.data.getIp().toLowerCase();
		if (server.endsWith("griefergames.net") || server.endsWith("griefergames.de")) {
			onGrieferGames = true;
		}
	}

	@EventListener
	public void onCitybuildJoin(CityBuildJoinEvent event) {
		onCitybuild = true;
	}

	@EventListener
	public void onServerSwitch(ServerEvent.ServerSwitchEvent event) {
		onCitybuild = false;
	}

	@EventListener(priority = EventPriority.LOWEST)
	public void onServerQuit(ServerEvent.ServerQuitEvent event) {
		onGrieferGames = false;
	}

}
