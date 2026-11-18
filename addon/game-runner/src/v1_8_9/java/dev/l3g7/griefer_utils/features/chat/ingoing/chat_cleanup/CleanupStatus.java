/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.ingoing.chat_cleanup;

import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.misc.server.Remote;
import dev.l3g7.griefer_utils.core.api.util.io.IO;
import dev.l3g7.griefer_utils.core.events.MessageEvent.RawMessageModifyEvent;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.util.IChatComponentUtil;
import dev.l3g7.griefer_utils.core.util.PlayerUtil;
import dev.l3g7.griefer_utils.features.chat.ingoing.chat_cleanup.ChatCleanup.NewsOrStatusMode;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.features.chat.ingoing.chat_cleanup.ChatCleanup.NewsOrStatusMode.NONE;
import static dev.l3g7.griefer_utils.features.chat.ingoing.chat_cleanup.ChatCleanup.NewsOrStatusMode.NORMAL;

public class CleanupStatus {

	private static final Remote<Status[]> statuses = new Remote<>("statuses", new Status[0], v -> {
		var statuses = IO.GSON.fromJson(v, StatusLine[][].class);

		var parsed = new Status[statuses.length];
		for (int i = 0; i < statuses.length; i++)
			parsed[i] = new Status(statuses[i], i);

		return parsed;
	});

	private static Status activeStatus;
	private static Player activePlayer;
	private static Integer activeLine = -1;

	static final DropDownSetting<NewsOrStatusMode> displayType = DropDownSetting.create(NewsOrStatusMode.class)
		.name("Status")
		.description("Ändert die Darstellung der Status.")
		.icon("players")
		.defaultValue(NORMAL)
		.inferConfig("status");

	private static Matcher initStatusLine(RawMessageModifyEvent event) {
		String formattedText = event.message.getFormattedText();

		if (activeStatus == null) {
			// Status start
			for (Status status : statuses.get()) {
				Matcher matcher = status.patterns[0].matcher(formattedText);
				if (matcher.matches()) {
					activeStatus = status;
					activeLine = 0;
					return matcher;
				}
			}
			return null;
		}

		while (!checkStatusEnd()) {
			// Status continuation
			activeLine++;

			Matcher matcher = activeStatus.patterns[activeLine].matcher(formattedText);
			if (matcher.matches()) {
				return matcher;
			} else if (!activeStatus.lines[activeLine].optional()) {
				BugReporter.reportError("Status mismatch: At " + activeStatus.ref() + " / " + activeLine + ", got " + formattedText);
				activeStatus = null;
				return null;
			} else {
				if (checkStatusEnd())
					return initStatusLine(event); // Check end of status + new
			}
		}

		return null; // Should never happen
	}

	@EventListener
	private static void onMessageReceive(RawMessageModifyEvent event) {
		if (!ChatCleanup.get().isEnabled() || displayType.get() == NORMAL)
			return;

		Matcher matcher = initStatusLine(event);
		if (activeStatus == null || matcher == null)
			return;

		if (activeStatus.lines[activeLine].hasPlayer)
			activePlayer = new Player(event.message, matcher.start("player"), matcher.end("player"));

		modifyStatus(event, matcher);
		checkStatusEnd();
	}

	private static boolean checkStatusEnd() {
		if (activeStatus.lines.length <= activeLine + 1) {
			activeStatus = null;
			return true;
		} else
			return false;
	}

	private static void modifyStatus(RawMessageModifyEvent event, Matcher matcher) {
		if (displayType.get() == NONE || !activeStatus.lines[activeLine].hasMessage || matcher == null) {
			event.dropMessage();
			return;
		}

		int playerStart = activePlayer.start;
		int playerEnd = activePlayer.end;
		int messageStart = matcher.start("message");
		int messageEnd = matcher.end("message");

		if (activePlayer.sourceLine == event.message) {
			// Check for suffix
			String playerName = event.message.getFormattedText().substring(matcher.start("name"), matcher.end("name")).replaceAll("§.", "");
			String suffix = PlayerUtil.getSuffix(playerName);
			if (suffix != null) {
				String message = event.message.getFormattedText().substring(messageStart).replaceAll("§.", "");
				if (message.equals(suffix)) {
					// Empty real message
					playerEnd = messageEnd;
					messageEnd = messageStart; // 0 width
				} else if (message.startsWith(suffix + " ")) {
					int messageStartUnfmt = IChatComponentUtil.formattedIndexToUnformatted(event.message, messageStart);
					messageStartUnfmt += suffix.length();

					messageStart = IChatComponentUtil.unformattedIndexToFormatted(event.message, messageStartUnfmt + 1); // no " "
					playerEnd = IChatComponentUtil.unformattedIndexToFormatted(event.message, messageStartUnfmt);
					activePlayer = new Player(activePlayer.sourceLine, playerStart, playerEnd);
				}
			}
		}

		IChatComponent message = IChatComponentUtil.extractFromFormatted(event.message, messageStart, messageEnd);
		IChatComponent player = IChatComponentUtil.extractFromFormatted(activePlayer.sourceLine, playerStart, playerEnd);

		ChatComponentText flag = new ChatComponentText("⚐");
		flag.getChatStyle().setBold(true).setColor(EnumChatFormatting.GOLD);

		event.setMessage(
			new ChatComponentText("") // Simulate GG's formatting
				.appendSibling(flag)
				.appendText(" ")
				.appendSibling(player)
				.appendText(" ")
				.appendSibling(message)
		);
	}

	public record StatusLine(String pattern, boolean hasPlayer, boolean hasMessage, boolean optional) {}

	private record Player(IChatComponent sourceLine, int start, int end) {}

	private record Status(StatusLine[] lines, Pattern[] patterns, int ref) {
		public Status(StatusLine[] lines, int ref) {
			this(lines, compile(lines), ref);
		}

		private static Pattern[] compile(StatusLine[] lines) {
			Pattern[] compiled = new Pattern[lines.length];
			for (int j = 0; j < lines.length; j++)
				compiled[j] = Pattern.compile(lines[j].pattern);

			return compiled;
		}
	}

}
