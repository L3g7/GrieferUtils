/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat;

import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import net.minecraft.init.Items;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.display;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

@Singleton
public class BetterSwitchCommand extends Feature {

	private static final Pattern COMMAND_PATTERN = Pattern.compile("^/(?:cb|switch) ?(?:cb)?(\\w+)(?: (.*))?$", Pattern.CASE_INSENSITIVE);

	private static Citybuild targetCitybuild = null;
	private static String command = null;

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("/switch verbessern")
		.description("Verbessert den '/switch <cb>' Befehl durch Aliasse und einem optionalem Join-Text. (z.B. '/cbe Hallo')", "Der Join-Text wird nach dem Beitreten automatisch in den Chat geschrieben.", "", "Für Hilfe gib '/cb' ein.")
		.icon(Items.compass);

	@EventListener
	public void onMessageSend(MessageEvent.MessageSendEvent event) {
		if (!ServerCheck.isOnGrieferGames())
			return;

		String msg = event.message;

		Matcher matcher = COMMAND_PATTERN.matcher(msg);

		if (matcher.matches()) {
			event.cancel();

			Citybuild cb = Citybuild.getCitybuild(matcher.group(1));
			if (cb != Citybuild.ANY) {
				cb.join();
				targetCitybuild = cb;
				command = matcher.group(2);
				return;
			}
		}

		if ((!msg.startsWith("/cb") && !msg.startsWith("/switch")) || msg.equals("/switch"))
			return;

		display(Constants.ADDON_PREFIX + "Syntax: '/switch <CB> [text]', '/cb <CB> [text]' oder '/cb<CB> [text]'.");
		display(Constants.ADDON_PREFIX + "§f§nAliasse:");

		display(Constants.ADDON_PREFIX + "§7Nature: 'n'");
		display(Constants.ADDON_PREFIX + "§7Extreme: 'x'");
		display(Constants.ADDON_PREFIX + "§7Evil: 'e'");
		display(Constants.ADDON_PREFIX + "§7Wasser: 'w'");
		display(Constants.ADDON_PREFIX + "§7Lava: 'l'");
		display(Constants.ADDON_PREFIX + "§7Event: 'v'");

		event.cancel();
	}

	@EventListener(triggerWhenDisabled = true)
	public void onCitybuild(CitybuildJoinEvent event) {
		if (command == null)
			return;

		if (!targetCitybuild.matches(getServerFromScoreboard())) {
			command = null;
			targetCitybuild = null;
			return;
		}

		if (!MessageEvent.MessageSendEvent.post(command))
			player().sendChatMessage(command);
		command = null;
	}

	public static void sendOnCitybuild(String command, Citybuild cb) {
		if (cb == Citybuild.ANY)
			return;

		if (cb.isOnCb()) {
			send(command);
			return;
		}

		cb.join();
		targetCitybuild = cb;
		BetterSwitchCommand.command = command;
	}

}
