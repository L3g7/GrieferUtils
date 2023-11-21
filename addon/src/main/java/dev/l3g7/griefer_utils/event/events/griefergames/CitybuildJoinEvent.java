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

package dev.l3g7.griefer_utils.event.events.griefergames;

import dev.l3g7.griefer_utils.core.event_bus.Event;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent;

/**
 * An event being posted after joining a city build.
 */
public class CitybuildJoinEvent extends Event {

	private static boolean waitingForAuth = false;
	private static boolean dataWasLoaded = false;

	@EventListener
	private static void onServerSwitch(ServerEvent.ServerSwitchEvent event) {
		waitingForAuth = dataWasLoaded = false;
	}

	@EventListener
	private static void onMessage(MessageReceiveEvent event) {
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

}
