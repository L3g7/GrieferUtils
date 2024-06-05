/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat;

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event.event_bus.Priority;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageModifyEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.*;

@Singleton
public class ChatMods extends Feature {

	private static final List<String> MYSTERY_MOD_DOWNLOAD_NOTIFICATION = ImmutableList.of(
		"§r§8[§r§6GrieferGames§r§8] §r§cOhje. Du benutzt noch kein MysteryMod!§r",
		"§r§8[§r§6GrieferGames§r§8] §r§fWir sind optimiert für MysteryMod und die neusten Funktionen hast Du nur damit.§r",
		"§r§8[§r§6GrieferGames§r§8] §r§fDownload: §r§ahttps://mysterymod.net/download/§r",
		"§r§8[§r§6GrieferGames§r§8] §r§fWir sind optimiert für MysteryMod. Lade Dir gerne die Mod runter!§r"
	);

	private static final String RAINBOW_COLORS = "c6eabd";

	private final SwitchSetting antiClearChat = SwitchSetting.create()
		.name("Clearchat unterbinden")
		.description("Verhindert das leeren des Chats durch /clearchat.")
		.icon(Blocks.barrier)
		.defaultValue(true);

	private final SwitchSetting removeSupremeSpaces = SwitchSetting.create()
		.name("Supreme-Leerzeichen entfernen")
		.description("Entfernt die Leerzeilen vor und nach Nachrichten von Spielern mit Supreme-Rang.")
		.icon(Blocks.barrier)
		.defaultValue(true);

	private final SwitchSetting removeStreamerNotifications = SwitchSetting.create()
		.name("Streamer-Benachrichtigungen entfernen")
		.description("Unterdrückt Benachrichtigungen über Livestreams.")
		.icon("twitch");

	private final SwitchSetting removeMysteryMod = SwitchSetting.create()
		.name("Download-Benachrichtigungen entfernen")
		.description("Unterdrückt Erinnerungen an den Download MysteryMods.")
		.icon("mysterymod")
		.defaultValue(true);

	private final SwitchSetting removeLuckyBlock = SwitchSetting.create()
		.name("LuckyBlock-Benachrichtigungen entfernen")
		.description("Unterdrückt Benachrichtigungen über LuckyBlock-Gewinne.")
		.icon(ItemUtil.createItem(Blocks.gold_block, 0, true));

	private final SwitchSetting removeCaseOpening = SwitchSetting.create()
		.name("CaseOpening-Benachrichtigungen entfernen")
		.description("Unterdrückt Benachrichtigungen über CaseOpening-Gewinne.")
		.icon("chest");

	private final DropDownSetting<NewsMode> news = DropDownSetting.create(NewsMode.class)
		.name("News")
		.description("Ändert die Darstellung von News.")
		.icon("labymod_3/exclamation_mark")
		.defaultValue(NewsMode.NORMAL);

	private final SwitchSetting antiRainbow = SwitchSetting.create()
		.name("Rainbow-Farben entfernen")
		.description("Entfernt die Farben von Nachrichten mit Rainbow-Schrift.")
		.icon("labymod_3/tabping_colored");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Chat aufräumen")
		.icon("speech_bubble")
		.description("Räumt den Chat auf.")
		.subSettings(antiClearChat, removeSupremeSpaces, removeStreamerNotifications, removeMysteryMod, removeLuckyBlock, removeCaseOpening, news, antiRainbow, LabyBridge.labyBridge.createLaby3DropDownPadding());

	private boolean isNews = false;

	@EventListener
	public void onMessageReceive(MessageReceiveEvent event) {
		if (shouldCancel(event.message.getFormattedText()))
			event.cancel();
	}

	@EventListener(priority = Priority.HIGH)
	public void onMessageModify(MessageModifyEvent event) {
		if (!antiRainbow.get())
			return;

		for (Pattern pattern : new Pattern[] {GLOBAL_RECEIVE_PATTERN, MESSAGE_RECEIVE_PATTERN, MESSAGE_SEND_PATTERN, PLOTCHAT_RECEIVE_PATTERN}) {
			Matcher matcher = pattern.matcher(event.original.getFormattedText());
			if (!matcher.matches())
				continue;

			String message = matcher.group("message");
			String msg = message.replace("§l", "").replace("§r", "").replaceAll("§. ", "");
			if (msg.length() % 3 != 0)
				return;

			for (int i = 0; i < msg.length() / 3; i++) {
				int index = i * 3;

				if (msg.charAt(index) != '§' || msg.charAt(++index) != RAINBOW_COLORS.charAt(i % 6))
					return;
			}

			IChatComponent messageICC = new ChatComponentText(message.replaceAll("§.", ""));
			messageICC.getChatStyle().setBold(true).setColor(EnumChatFormatting.AQUA);
			event.setMessage(new ChatComponentText(event.original.getFormattedText().substring(0, matcher.start("message"))).appendSibling(messageICC));
		}
	}

	private boolean shouldCancel(String formattedText) {
		boolean isNewsLine = formattedText.equals("§f§m------------§r§8 [ §r§6News§r§8 ] §r§f§m------------§r");
		if (isNewsLine)
			isNews = !isNews;

		// News mode
		if (news.get() == NewsMode.NONE && (isNews || isNewsLine))
			return true;
		else if (news.get() == NewsMode.COMPACT && (isNewsLine || (isNews && formattedText.trim().equals("§r§8\u00bb§r"))))
			return true;

		// Anti clear chat
		if (antiClearChat.get() && formattedText.replaceAll("§.", "").trim().isEmpty())
			return true;

		// remove supreme spaces
		if (removeSupremeSpaces.get() && formattedText.trim().equals("§r§8\u00bb§r"))
			return true;

		// remove streamer
		if (removeStreamerNotifications.get() && formattedText.startsWith("§r§8[§6Streamer§8]"))
			return true;

		// Remove MysteryMod download notification
		if (removeMysteryMod.get() && MYSTERY_MOD_DOWNLOAD_NOTIFICATION.contains(formattedText))
			return true;

		// remove luckyblock
		if (removeLuckyBlock.get() && formattedText.startsWith("§r§8[§r§e§lLu§r§6§lck§r§e§lyB§r§6§llo§r§e§lck§r§8]"))
			return true;

		// remove case opening
		return removeCaseOpening.get() && formattedText.startsWith("§r§8[§r§bCase§r§fOpening§r§8]");
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
