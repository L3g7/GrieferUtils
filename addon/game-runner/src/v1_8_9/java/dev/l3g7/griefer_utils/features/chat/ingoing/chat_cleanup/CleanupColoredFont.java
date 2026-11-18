/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.ingoing.chat_cleanup;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageModifyEvent;
import dev.l3g7.griefer_utils.core.events.StaticDataReceiveEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.IChatComponentUtil;
import dev.l3g7.griefer_utils.core.util.IChatComponentUtil.MutableComponent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.event_bus.Priority.HIGH;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.*;
import static net.minecraft.util.EnumChatFormatting.AQUA;

public class CleanupColoredFont {

	private static List<String> COLORED_FONTS = ImmutableList.of();
	private static final Pattern SINGLE_COLORED_FONT_PATTERN = Pattern.compile("^§(.)§l[^§]+$");

	static final SwitchSetting enabled = SwitchSetting.create()
		.name("Farbige Schrift entfernen")
		.description("Entfernt die Farben von Nachrichten mit farbiger Schrift §8(/schrift)§r.")
		.icon("tabping_colored")
		.inferConfig("anti_colored_font");

	@EventListener
	private static void onStaticData(StaticDataReceiveEvent event) {
		COLORED_FONTS = ImmutableList.copyOf(event.data.coloredFonts);
	}

	@EventListener(priority = HIGH)
	public static void removeColoredFont(MessageModifyEvent event) {
		if (!ChatCleanup.get().isEnabled() || !enabled.get())
			return;

		for (Pattern pattern : new Pattern[]{GLOBAL_CHAT_PATTERN, GLOBAL_RECEIVE_PATTERN, MESSAGE_RECEIVE_PATTERN, MESSAGE_SEND_PATTERN, PLOTCHAT_RECEIVE_PATTERN, CLANCHAT_RECEIVE_PATTERN}) {
			Matcher matcher = pattern.matcher(event.message.getFormattedText());
			if (!matcher.matches())
				continue;

			String message = matcher.group("message");
			String msg = message.replace("§r", "").replaceAll("(§.)* ", "");
			if (!usesFont(msg))
				return;

			int messageStart = matcher.start("message");
			IChatComponent startICC = event.message.createCopy();
			int length = ChatCleanup.getFormattedLength(startICC);

			for (MutableComponent component : IChatComponentUtil.getNestedSiblings(startICC)) {
				if (length >= messageStart) {
					component.remove();
					continue;
				}

				length += component.getFormattedSubstring(0, component.getText().length()).length();
			}

			IChatComponent messageICC = new ChatComponentText(message.replaceAll("§.", ""));
			messageICC.getChatStyle().setBold(true).setColor(AQUA);

			startICC.appendSibling(messageICC);
			event.setMessage(startICC);
			return;
		}
	}

	private static boolean usesFont(String msg) {
		Matcher singleColoredFontMatcher = SINGLE_COLORED_FONT_PATTERN.matcher(msg);
		if (singleColoredFontMatcher.matches())
			return COLORED_FONTS.contains(singleColoredFontMatcher.group(1));

		msg = msg.replace("§l", "");
		if (msg.length() % 3 != 0)
			return false;

		List<String> fonts = new ArrayList<>(COLORED_FONTS);
		for (int i = 0; i < msg.length() / 3 && !fonts.isEmpty(); i++) {
			int index = i * 3;

			if (msg.charAt(index++) != '§')
				return false;

			Iterator<String> it = fonts.iterator();
			while (it.hasNext()) {
				String font = it.next();
				if (msg.charAt(index) != font.charAt(i % font.length()))
					it.remove();
			}
		}

		return !fonts.isEmpty();
	}

}
