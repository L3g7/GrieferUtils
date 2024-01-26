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

package dev.l3g7.griefer_utils.v1_8_9.events.griefergames;

import dev.l3g7.griefer_utils.api.event.event_bus.Event;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.misc.Citybuild;
import dev.l3g7.griefer_utils.v1_8_9.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.v1_8_9.misc.ServerCheck;
import net.minecraft.network.play.server.S3EPacketTeams;

/**
 * An event being posted after successfully joining a citybuild. (When the player's data has been loaded.)
 */
public class CitybuildJoinEvent extends Event {

	private static boolean waitingForAuth = false;
	private static boolean dataWasLoaded = false;

	@EventListener
	private static void onServerSwitch(ServerSwitchEvent event) {
		waitingForAuth = dataWasLoaded = false;
	}

	@EventListener
	private static void onMessage(MessageReceiveEvent event) {
		if (!ServerCheck.isOnGrieferGames())
			return;

		if (event.message.getFormattedText().startsWith("§r§8[§r§6GGAuth§r§8] §r§7Bitte verifiziere dich"))
			waitingForAuth = true;

		if (event.message.getFormattedText().equals("§r§8[§r§6GGAuth§r§8] §r§aDu wurdest erfolgreich verifiziert.§r")) {
			waitingForAuth = false;
			fireIfReady();
		}

		if (event.message.getFormattedText().equals("§r§8[§r§6GrieferGames§r§8] §r§aDeine Daten wurden vollständig heruntergeladen.§r")) {
			dataWasLoaded = true;
			fireIfReady();
		}
	}

	private static void fireIfReady() {
		if (!dataWasLoaded || waitingForAuth)
			return;

		new CitybuildJoinEvent().fire();
		dataWasLoaded = false;
	}

	/**
	 * An event being posted as early as possible after joining a citybuild.<br>
	 * The player's data has not been loaded at this point.
	 */
	public static class Early extends CitybuildJoinEvent {

		private static boolean switchedServer = false;
		public final Citybuild citybuild;

		private Early(Citybuild citybuild) {
			this.citybuild = citybuild;
		}

		@EventListener
		private static void onServerSwitch(ServerSwitchEvent event) {
			if (ServerCheck.isOnGrieferGames())
				switchedServer = true;
		}

		@EventListener
		private static void onTeamsPacket(PacketEvent.PacketReceiveEvent<S3EPacketTeams> event) {
			if (!switchedServer || !event.packet.getName().equals("server_value") || event.packet.getPrefix().isEmpty())
				return;

			switchedServer = false;
			Citybuild cb = Citybuild.getCitybuild(event.packet.getPrefix().replaceAll("§.", ""));

			if (cb != Citybuild.ANY)
				new Early(cb).fire();
		}

	}

}
