/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.ingoing.chat_cleanup;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageModifyEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.event_bus.Priority.HIGH;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.*;
import static net.minecraft.util.EnumChatFormatting.AQUA;
import static net.minecraft.util.EnumChatFormatting.WHITE;

public class CleanupHeroHighlights {

	static final SwitchSetting enabled = SwitchSetting.create()
		.name("Hero-Hervorhebung entfernen")
		.description("Entfernt die Hervorhebung von @Namen von Spielern mit Hero Rang.")
		.icon("dye_white")
		.since("2.4-BETA-1")
		.inferConfig("remove_hero_highlights");

	@EventListener(priority = HIGH)
	public static void removeHighlights(MessageModifyEvent event) {
		if (!ChatCleanup.get().isEnabled() || !enabled.get())
			return;

		for (Pattern pattern : new Pattern[]{GLOBAL_CHAT_PATTERN, GLOBAL_RECEIVE_PATTERN, MESSAGE_RECEIVE_PATTERN, MESSAGE_SEND_PATTERN, PLOTCHAT_RECEIVE_PATTERN, CLANCHAT_RECEIVE_PATTERN}) {
			Matcher matcher = pattern.matcher(event.message.getFormattedText());
			if (!matcher.matches())
				continue;

			checkForHeroHighlights(event.message, new AtomicInteger(ChatCleanup.getFormattedLength(event.message)), matcher.start("message"), matcher.end("message"));
		}
	}

	private static boolean checkForHeroHighlights(IChatComponent icc, AtomicInteger cursor, int start, int end) {
		ListIterator<IChatComponent> iterator = icc.getSiblings().listIterator();

		while (iterator.hasNext()) {
			IChatComponent sibling = iterator.next();
			boolean isBeforeStart = cursor.get() < start;
			if (isBeforeStart)
				cursor.addAndGet(ChatCleanup.getFormattedLength(sibling));

			if (checkForHeroHighlights(sibling, cursor, start, end))
				return true;

			if (isBeforeStart)
				continue;

			boolean isStart = cursor.get() == start;
			cursor.addAndGet(ChatCleanup.getFormattedLength(sibling));

			if (sibling.getChatStyle().getColor() != WHITE || !sibling.getChatStyle().getBold())
				continue;

			String text = sibling.getUnformattedTextForChat();
			if (!text.startsWith("@") || !FORMATTED_PLAYER_NAME_PATTERN.matcher(text.substring(1)).matches())
				continue;

			// Hero highlight detected
			iterator.remove();

			if (!isStart && iterator.hasPrevious()) {
				if (iterator.previous() instanceof ChatComponentText cct) {
					// Merge with previous
					Reflection.set(cct, "text", cct.getUnformattedTextForChat() + text);
					return true;
				}
			}

			if (cursor.get() != end /* end of the message */ && iterator.hasNext()) {
				if (iterator.next() instanceof ChatComponentText cct) {
					// Merge with next
					Reflection.set(cct, "text", text + cct.getUnformattedTextForChat());
					return true;
				}
			}

			// Fallback
			iterator.add(sibling);
			sibling.getChatStyle().setBold(true);
			sibling.getChatStyle().setColor(AQUA);
		}

		return false;
	}

}
