/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.ingoing.chat_cleanup;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.util.IChatComponent;

import static dev.l3g7.griefer_utils.features.chat.ingoing.chat_cleanup.ChatCleanup.NewsOrStatusMode.*;

@Singleton
public class ChatCleanup extends Feature {

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

	private final DropDownSetting<NewsOrStatusMode> news = DropDownSetting.create(NewsOrStatusMode.class)
		.name("News")
		.description("Ändert die Darstellung von News.")
		.icon("enchanted_book")
		.defaultValue(NORMAL);

	private final SwitchSetting removeBroadcast = SwitchSetting.create()
		.name("Broadcasts entfernen")
		.description("Entfernt die Broadcast-Hervorhebung.")
		.icon("bell")
		.since("2.4-BETA-1");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Chat aufräumen")
		.icon("crossed_out_book")
		.description("Räumt den Chat auf.")
		.subSettings(
			antiClearChat, removeSupremeSpaces, removeStreamerNotifications, removeLuckyBlock, removeCaseOpening, removeBroadcast,
			CleanupHeroHighlights.enabled, CleanupColoredFont.enabled,
			HeaderSetting.create(),
			news
		);

	private boolean isNews = false;

	public static ChatCleanup get() {
		return get(ChatCleanup.class);
	}

	@EventListener
	public void onMessageReceive(MessageReceiveEvent event) {
		if (shouldCancel(event.message.getFormattedText(), event.message.getUnformattedText()))
			event.cancel();
	}

	private boolean shouldCancel(String formattedText, String unformattedText) {
		// News mode
		boolean isNewsLine = formattedText.equals("§f§m------------§r§8 [ §r§6News§r§8 ] §r§f§m------------§r");
		if (isNewsLine)
			isNews = !isNews;

		if (news.get() == NONE && (isNews || isNewsLine))
			return true;
		else if (news.get() == COMPACT && (isNewsLine || (isNews && formattedText.trim().equals("§r§8»§r"))))
			return true;

		// Anti clear chat
		if (antiClearChat.get() && unformattedText.trim().isEmpty())
			return true;

		// remove supreme spaces
		if (removeSupremeSpaces.get() && formattedText.trim().equals("§r§8»§r"))
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

	/**
	 * @return The IChatComponent's formatted length, without its siblings
	 */
	static int getFormattedLength(IChatComponent icc) {
		return icc.getChatStyle().getFormattingCode().length() +
			icc.getUnformattedTextForChat().length() +
			"§r".length();
	}

	enum NewsOrStatusMode implements Named {

		NORMAL("Normal"), COMPACT("Kompakt"), NONE("Versteckt");

		private final String name;

		NewsOrStatusMode(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

	}

}
