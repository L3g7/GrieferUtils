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

package dev.l3g7.griefer_utils.features;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.event.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.misc.ChatQueue;
import dev.l3g7.griefer_utils.misc.ServerCheck;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.misc.Constants.ADDON_PREFIX;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

/**
 * Might be replaced with something better if more (complex) commands are added.
 */
public class Commands {

	private static final String CMD_PREFIX = "/gu:";
	private static final List<String> onCbCommands = new ArrayList<>();

	@EventListener
	private static void onMessageSend(MessageSendEvent event) {
		if (!event.message.startsWith(CMD_PREFIX))
			return;

		event.cancel();

		String msg = event.message.substring(CMD_PREFIX.length());
		String[] parts = msg.split(" ");
		String response = processCommand(parts[0], msg.substring(parts[0].length()).trim());
		if (response != null)
			display(ADDON_PREFIX + "§c" + response);
	}

	private static String processCommand(String command, String argsString) {
		if (command.equalsIgnoreCase("run_on_cb")) {
			if (argsString.isEmpty())
				return "Usage: /gu:run_on_cb <text>";

			runOnCb(argsString);
			return null;
		}

		if (command.equalsIgnoreCase("queue")) {
			if (argsString.isEmpty())
				return "Usage: /gu:queue <text>";

			ChatQueue.send(argsString);
			return null;
		}

		if (command.equalsIgnoreCase("run_multiple")) {
			if (argsString.isEmpty())
				return "Usage: /gu:run_multiple <text>|<text>|...";

			for (String s : argsString.split("\\|"))
				if (!MessageSendEvent.post(s))
					player().sendChatMessage(s);

			return null;
		}

		if (command.equalsIgnoreCase("run_if_online")) {
			String[] parts = argsString.split(" ");
			if (parts.length < 2)
				return "Usage: /gu:run_if_online <Spieler> <text>";

			String player = parts[0];
			String cmd = argsString.substring(player.length() + 1);

			if (mc().getNetHandler().getPlayerInfo(player) != null)
				if (!MessageSendEvent.post(cmd))
					player().sendChatMessage(cmd);

			return null;
		}

		if (command.equalsIgnoreCase("help")) {
			display(ADDON_PREFIX + "Befehle:");
			display(ADDON_PREFIX + "/gu:help");
			display(ADDON_PREFIX + "/gu:run_on_cb <text>");
			display(ADDON_PREFIX + "/gu:queue <text>");
			display(ADDON_PREFIX + "/gu:run_multiple <text1>|<text2>|[text3]|...");
			display(ADDON_PREFIX + "/gu:run_if_online <Spieler> <text>");
			return null;
		}

		return "Unbekannter Befehl. (Siehe /gu:help)";
	}

	public static void runOnCb(String command) {
		if (ServerCheck.isOnCitybuild()) {
			if (!MessageEvent.MessageSendEvent.post(command))
				player().sendChatMessage(command);
		} else {
			onCbCommands.add(command);
		}
	}

	@EventListener
	private static void onCitybuild(CitybuildJoinEvent event) {
		if (onCbCommands.isEmpty())
			return;

		for (String command : onCbCommands)
			if (!MessageSendEvent.post(command))
				player().sendChatMessage(command);

		onCbCommands.clear();
	}

}
