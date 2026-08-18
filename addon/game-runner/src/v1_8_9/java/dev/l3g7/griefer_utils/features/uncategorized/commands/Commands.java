/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.commands;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.core.misc.ChatQueue;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import net.labymod.api.Laby;
import net.labymod.core.client.gui.screen.activity.activities.ingame.chat.input.tab.NameHistoryActivity;
import net.labymod.core.main.LabyMod;
import net.labymod.ingamechat.tabs.GuiChatNameHistory;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.util.List;
import java.util.Queue;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.ADDON_PREFIX;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.features.uncategorized.commands.Command.CommandBuilder.command;
import static dev.l3g7.griefer_utils.features.uncategorized.commands.Commands.CommandBridge.commandBridge;
import static net.labymod.api.Laby.labyAPI;

public class Commands {

	private static final String CMD_PREFIX = "/gu:";
	private static final List<String> onCbCommands = new ArrayList<>();
	private static final Map<String, Command> commands = new HashMap<>();
	private static final Timer TIMER = new Timer("GrieferUtils-Command-Timer", true);

	public static void registerCommand(Command command) {
		commands.put(command.base.toLowerCase(), command);
	}

	@EventListener
	private static void onMessageSend(MessageSendEvent event) {
		if (!event.message.startsWith(CMD_PREFIX))
			return;

		event.cancel();

		String msg = event.message.substring(CMD_PREFIX.length());
		Queue<String> input = new LinkedList<>(Arrays.asList(msg.split(" ")));

		String response = processCommand(input.remove(), input);
		if (response != null)
			display(ADDON_PREFIX + "§c" + response);
	}

	private static String processCommand(String base, Queue<String> input) {
		Command command = commands.get(base.toLowerCase());
		if (command == null)
			return "Unbekannter Befehl. (Siehe /gu:help)";

		try {
			if (!command.process(input))
				return "Verwendung: " + CMD_PREFIX + command;
		} catch (Throwable t) {
			t.printStackTrace();
			return t.toString();
		}

		return null;
	}

	private static void trySend(String command) {
		if (player() != null && !MessageSendEvent.post(command))
			player().sendChatMessage(command);
	}

	public static void runOnCb(String command) {
		if (ServerCheck.isOnCitybuild()) {
			trySend(command);
		} else {
			onCbCommands.add(command);
		}
	}

	@EventListener
	private static void onCitybuild(CitybuildJoinEvent event) {
		if (onCbCommands.isEmpty())
			return;

		if (player() == null) {
			TickScheduler.runNextClientTick(() -> onCitybuild(event));
			return;
		}

		List<String> cbCommands = new ArrayList<>(onCbCommands);
		for (String command : cbCommands)
			trySend(command);

		onCbCommands.removeAll(cbCommands);
	}

	static {
		registerCommand(command("help")
			.build(args -> {
				display(ADDON_PREFIX + "Befehle:");
				commands.values().stream()
					.sorted(Comparator.comparing(c -> c.base))
					.forEachOrdered(command -> display(ADDON_PREFIX + CMD_PREFIX + command));
			}));

		registerCommand(command("run_on_cb")
			.greedyString("Text")
			.build(args -> runOnCb(args.get("Text"))));

		registerCommand(command("queue")
			.greedyString("Text")
			.build(args -> {
				String text = args.get("Text");
				if (!MessageSendEvent.post(text))
					ChatQueue.send(text);
			}));

		registerCommand(command("run_multiple")
			.greedyString("<Text1>|<Text2>|...")
			.build(args -> {
				String text = args.get("<Text1>|<Text2>|...");
				for (String s : text.split("\\|"))
					trySend(s);
			}));

		registerCommand(command("run_if_online")
			.stringArg("Spieler")
			.greedyString("Text")
			.build(args -> {
				String command = args.get("Text");

				if (mc().getNetHandler().getPlayerInfo((String) args.get("Spieler")) != null)
					trySend(command);
			}));

		registerCommand(command("schedule")
			.longArg("Delay (ms)")
			.greedyString("Befehl")
			.build(args -> {
				String command = args.get("Befehl");
				TIMER.schedule(new TimerTask() {
					@Override
					public void run() {
						trySend(command);
					}
				}, (long) args.get("Delay (ms)"));
			}));

		registerCommand(command("notify")
			.greedyString("<Titel>|<Nachricht>")
			.build(args -> {
				String string = args.get("<Titel>|<Nachricht>");
				String[] parts = string.replace('&', '§').split("\\|");
				if (parts.length != 2) {
					display(ADDON_PREFIX + "§cVerwendung: " + CMD_PREFIX + "notify <Titel>|<Nachricht>");
					return;
				}

				LabyBridge.labyBridge.notify(parts[0], parts[1]);
			}));

		registerCommand(command("name_history")
			.stringArg("Spieler")
			.build(args -> {
				String name = args.get("Spieler");

				if (name.startsWith("!")) {
					labyBridge.notifyMildError("Von Bedrock-Spielern kann kein Namensverlauf abgefragt werden.");
					return;
				}

				if (!Constants.UNFORMATTED_JAVA_PLAYER_NAME_PATTERN.matcher(name).matches()) {
					labyBridge.notifyMildError("Ungültiger Spielername.");
					return;
				}

				commandBridge.openNameHistory(name);
			}));

		registerCommand(command("copy")
			.greedyString("Text")
			.build(args -> {
				String text = args.get("Text");

				commandBridge.copy(text);
				labyBridge.notify("\"" + text + "\"", "wurde in die Zwischenablage kopiert.");
			}));

	}

	@Bridged
	public interface CommandBridge {
		CommandBridge commandBridge = FileProvider.getBridge(CommandBridge.class);

		void openNameHistory(String name);

		void copy(String text);
	}

	@Bridge
	@Singleton
	@ExclusiveTo(LABY_3)
	private static class CommandBridgeLaby3 implements CommandBridge {
		@Override
		public void openNameHistory(String name) {
			mc().displayGuiScreen(new GuiChatNameHistory("", name));
		}

		@Override
		public void copy(String text) {
			StringSelection selection = new StringSelection(text);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
		}
	}

	@Bridge
	@Singleton
	@ExclusiveTo(LABY_4)
	private static class CommandBridgeLaby4 implements CommandBridge {
		@Override
		public void openNameHistory(String name) {
			NameHistoryActivity activity = LabyMod.references().nameHistoryActivity();
			activity.scheduleQuery(name);
			labyAPI().minecraft().minecraftWindow().displayScreen(activity);
		}

		@Override
		public void copy(String text) {
			Laby.labyAPI().minecraft().chatExecutor().copyToClipboard(text);
		}
	}

}
