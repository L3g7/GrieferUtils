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

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.event.events.griefergames.CityBuildJoinEvent;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.display;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;

/**
 * Might be replaced with something better if more commands are added.
 */
public class Commands {

	private static final String CMD_PREFIX = "/gu:";
	private static final List<String> onCbCommands = new ArrayList<>();

	@EventListener
	private static void onMessageSend(MessageSendEvent event) {
		if (!event.message.startsWith(CMD_PREFIX))
			return;

		event.setCanceled(true);

		String msg = event.message.substring(CMD_PREFIX.length());
		String[] parts = msg.split(" ");
		String response = processCommand(parts[0], msg.substring(parts[0].length()).trim());
		if (response != null)
			display("§c" + response);
	}

	private static String processCommand(String command, String argsString) {
		if (command.equalsIgnoreCase("run_on_cb")) {
			if (argsString.isEmpty())
				return "Syntax: /gu:run_on_cb <Text>";

			if (ServerCheck.isOnCitybuild()) {
				if (!MinecraftForge.EVENT_BUS.post(new MessageSendEvent(command)))
					player().sendChatMessage(argsString);
			} else {
				onCbCommands.add(argsString);
			}
			return null;
		}

		return "Unbekannter Befehl";
	}

	@EventListener
	private static void onCityBuild(CityBuildJoinEvent event) {
		if (onCbCommands.isEmpty())
			return;

		for (String command : onCbCommands)
			if (!MinecraftForge.EVENT_BUS.post(new MessageSendEvent(command)))
				player().sendChatMessage(command);

		onCbCommands.clear();
	}

}
