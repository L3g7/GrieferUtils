/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.core.misc.ChatQueue;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.ADDON_PREFIX;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

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

			if (!MessageSendEvent.post(argsString))
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
				return "Usage: /gu:run_if_online <spieler> <text>";

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
			if (!MessageSendEvent.post(command))
				player().sendChatMessage(command);
		} else {
			onCbCommands.add(command);
		}
	}

	@EventListener
	private static void onCitybuild(CitybuildJoinEvent event) {
		if (onCbCommands.isEmpty())
			return;

		if (player() == null) {
			TickScheduler.runAfterClientTicks(() -> onCitybuild(event), 1);
			return;
		}

		for (String command : onCbCommands)
			if (!MessageSendEvent.post(command))
				player().sendChatMessage(command);

		onCbCommands.clear();
	}

}
