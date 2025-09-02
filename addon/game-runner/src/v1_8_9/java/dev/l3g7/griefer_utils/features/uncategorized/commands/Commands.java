/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.commands;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.core.misc.ChatQueue;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.features.uncategorized.scripts.ConstantParser;
import dev.l3g7.griefer_utils.features.uncategorized.scripts.Scripts;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.*;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.ADDON_PREFIX;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.features.uncategorized.commands.Command.CommandBuilder.command;

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
		if (!MessageSendEvent.post(command))
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
			TickScheduler.runAfterClientTicks(() -> onCitybuild(event), 1);
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
			.longArg("Delay")
			.greedyString("command")
			.build(args -> {
				String command = args.get("command");
				TIMER.schedule(new TimerTask() {
					@Override
					public void run() {
						trySend(command);
					}
				}, (long) args.get("Delay"));
			}));

		registerCommand(command("script")
			.greedyString("<Datei> [Args]")
			.build(args -> {
				String stuff = args.get("<Datei> [Args]");
				Iterator<String> parts = Arrays.asList(stuff.split(" ")).iterator();

				String file = parts.next();
				if (file.startsWith("\"")) {
					Object constant = ConstantParser.readConstant(file, parts);
					if (constant == null) {
						display(ADDON_PREFIX + "§cUngültiger Dateipfad");
						return;
					}

					file = constant.toString();
				}

				List<String> scriptArgs = new ArrayList<>();
				parts.forEachRemaining(scriptArgs::add);

				Throwable error;

				try {
					Scripts.run(Paths.get(file), scriptArgs.toArray(new String[0]));
					return;
				} catch (Scripts.ScriptNotFoundException s) {
					display(ADDON_PREFIX + "§cDas Script konnte nicht gefunden werden!");
					return;
				} catch (VerifyError v) {
					v.printStackTrace();
					display(ADDON_PREFIX + "§cUngültiger Bytecode: " + v.getMessage().split("\n")[0]);
					return;
				} catch (InvocationTargetException e) {
					error = e.getCause();
				} catch (Throwable t) {
					error = t;
				}

				error.printStackTrace();
				display(ADDON_PREFIX + "§c" + error.getClass().getSimpleName() + (error.getMessage() == null ? "" : (": " + error.getMessage())));
			}));
	}

}
