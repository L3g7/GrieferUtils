/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.ingoing;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageModifyEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.StaticDataReceiveEvent;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.IChatComponentUtil;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.*;

@Singleton
public class ChatCleanup extends Feature {

	private List<String> COLORED_FONTS = ImmutableList.of();
	private static final Pattern SINGLE_COLORED_FONT_PATTERN = Pattern.compile("^§(.)§l[^§]+$");

	private final SwitchSetting antiClearChat = SwitchSetting.create()
		.name("Clearchat unterbinden")
		.description("Verhindert das leeren des Chats durch /clearchat.")
		.icon("barrier")
		.defaultValue(true);

	private final SwitchSetting removeSupremeSpaces = SwitchSetting.create()
		.name("Supreme-Leerzeichen entfernen")
		.description("Entfernt die Leerzeilen vor und nach Nachrichten von Spielern mit Supreme-Rang.")
		.icon("barrier")
		.defaultValue(true);

	private final SwitchSetting removeStreamerNotifications = SwitchSetting.create()
		.name("Streamer-Benachrichtigungen entfernen")
		.description("Unterdrückt Benachrichtigungen über Livestreams.")
		.icon("twitch");

	private final SwitchSetting removeLuckyBlock = SwitchSetting.create()
		.name("LuckyBlock-Benachrichtigungen entfernen")
		.description("Unterdrückt Benachrichtigungen über LuckyBlock-Gewinne.")
		.icon("lucky_block");

	private final SwitchSetting removeCaseOpening = SwitchSetting.create()
		.name("CaseOpening-Benachrichtigungen entfernen")
		.description("Unterdrückt Benachrichtigungen über CaseOpening-Gewinne.")
		.icon("chest_golden");

	private final DropDownSetting<NewsMode> news = DropDownSetting.create(NewsMode.class)
		.name("News")
		.description("Ändert die Darstellung von News.")
		.icon("enchanted_book")
		.defaultValue(NewsMode.NORMAL);

	private final SwitchSetting removeBroadcast = SwitchSetting.create()
		.name("Broadcasts entfernen")
		.description("Entfernt die Broadcast-Hervorhebung.")
		.icon("bell")
		.since("2.4-BETA-1");

	private final SwitchSetting removeHeroHighlights = SwitchSetting.create()
		.name("Hero Hervorhebung entfernen")
		.description("Entfernt die Hervorhebung von @Namen von Spielern mit Hero Rang.")
		.icon("dye_white")
		.since("2.4-BETA-1");

	private final SwitchSetting antiColoredFont = SwitchSetting.create()
		.name("Farbige Schrift entfernen")
		.description("Entfernt die Farben von Nachrichten mit farbiger Schrift §8(/schrift)§r.")
		.icon("tabping_colored");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Chat aufräumen")
		.icon("crossed_out_book")
		.description("Räumt den Chat auf.")
		.subSettings(antiClearChat, removeSupremeSpaces, removeStreamerNotifications, removeLuckyBlock, removeCaseOpening, news, removeBroadcast, removeHeroHighlights, antiColoredFont);

	private boolean isNews = false;

	@EventListener
	private void onStaticData(StaticDataReceiveEvent event) {
		COLORED_FONTS = ImmutableList.copyOf(event.data.coloredFonts);
	}

	@EventListener
	public void onMessageReceive(MessageReceiveEvent event) {
		if (shouldCancel(event.message.getFormattedText(), event.message.getUnformattedText()))
			event.cancel();
	}

	@EventListener(priority = Priority.HIGH)
	public void removeColoredFont(MessageModifyEvent event) {
		if (!antiColoredFont.get() && !removeHeroHighlights.get())
			return;

		for (Pattern pattern : new Pattern[]{GLOBAL_CHAT_PATTERN, GLOBAL_RECEIVE_PATTERN, MESSAGE_RECEIVE_PATTERN, MESSAGE_SEND_PATTERN, PLOTCHAT_RECEIVE_PATTERN, CLANCHAT_RECEIVE_PATTERN}) {
			Matcher matcher = pattern.matcher(event.message.getFormattedText());
			if (!matcher.matches())
				continue;

			String message = matcher.group("message");
			if (removeHeroHighlights.get() && checkForHeroHighlights(event.message, new AtomicInteger(getFormattedLength(event.message)), matcher.start("message"), matcher.end("message")))
				return;

			if (!antiColoredFont.get())
				continue;

			String msg = message.replace("§r", "").replaceAll("(§.)* ", "");
			if (!usesFont(msg))
				return;

			int messageStart = matcher.start("message");
			IChatComponent startICC = event.message.createCopy();
			int length = getFormattedLength(startICC);

			for (IChatComponentUtil.MutableComponent component : IChatComponentUtil.getNestedSiblings(startICC)) {
				if (length >= messageStart) {
					component.remove();
					continue;
				}

				length += component.getFormattedSubstring(0, component.getText().length()).length();
			}

			IChatComponent messageICC = new ChatComponentText(message.replaceAll("§.", ""));
			messageICC.getChatStyle().setBold(true).setColor(EnumChatFormatting.AQUA);

			startICC.appendSibling(messageICC);
			event.setMessage(startICC);
			return;
		}
	}

	private boolean usesFont(String msg) {
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

	private static boolean checkForHeroHighlights(IChatComponent icc, AtomicInteger cursor, int start, int end) {
		ListIterator<IChatComponent> iterator = icc.getSiblings().listIterator();

		while (iterator.hasNext()) {
			IChatComponent sibling = iterator.next();
			boolean isBeforeStart = cursor.get() < start;
			if (isBeforeStart)
				cursor.addAndGet(getFormattedLength(sibling));

			if (checkForHeroHighlights(sibling, cursor, start, end))
				return true;

			if (isBeforeStart)
				continue;

			boolean isStart = cursor.get() == start;
			cursor.addAndGet(getFormattedLength(sibling));

			if (sibling.getChatStyle().getColor() != EnumChatFormatting.WHITE || !sibling.getChatStyle().getBold())
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
			sibling.getChatStyle().setColor(EnumChatFormatting.AQUA);
		}

		return false;
	}

	/**
	 * @return The IChatComponent's formatted length, without its siblings
	 */
	private static int getFormattedLength(IChatComponent icc) {
		return icc.getChatStyle().getFormattingCode().length() +
			icc.getUnformattedTextForChat().length() +
			"§r".length();
	}

	private boolean shouldCancel(String formattedText, String unformattedText) {
		boolean isNewsLine = formattedText.equals("§f§m------------§r§8 [ §r§6News§r§8 ] §r§f§m------------§r");
		if (isNewsLine)
			isNews = !isNews;

		// News mode
		if (news.get() == NewsMode.NONE && (isNews || isNewsLine))
			return true;
		else if (news.get() == NewsMode.COMPACT && (isNewsLine || (isNews && formattedText.trim().equals("§r§8\u00bb§r"))))
			return true;

		// Anti clear chat
		if (antiClearChat.get() && unformattedText.trim().isEmpty())
			return true;

		// remove supreme spaces
		if (removeSupremeSpaces.get() && formattedText.trim().equals("§r§8\u00bb§r"))
			return true;

		// remove streamer
		if (removeStreamerNotifications.get() && formattedText.startsWith("§r§8[§6Streamer§8]"))
			return true;

		// remove luckyblock
		if (removeLuckyBlock.get() && formattedText.startsWith("§r§8[§r§e§lLu§r§6§lck§r§e§lyB§r§6§llo§r§e§lck§r§8]"))
			return true;

		// remove broadcast
		if (removeBroadcast.get() && formattedText.equals("§r§f§m------------§r §r§8[§r§c§lBroadcast§r§8] §r§f§m------------§r"))
			return true;

		// remove case opening
		return removeCaseOpening.get() && (formattedText.startsWith("§r§8[§r§bCase§r§fOpening§r§8] §r§f§lDer Spieler §r") || formattedText.startsWith("§r§8[§r§bCase§r§fOpening§r§8] §r§f§lFolgender Preis wurde gezogen: §r")
			|| (formattedText.startsWith("§r§8[§r§bCase§r§fOpening§r§8] ") && unformattedText.contains("hat eine Kiste geöffnet und")));
	}

	private enum NewsMode implements Named {

		NORMAL("Normal"), COMPACT("Kompakt"), NONE("Versteckt");

		private final String name;
		NewsMode(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

	}

}
