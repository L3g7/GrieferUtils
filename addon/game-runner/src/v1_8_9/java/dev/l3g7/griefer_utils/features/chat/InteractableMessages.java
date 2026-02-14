/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageModifyEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.*;
import static net.minecraft.event.ClickEvent.Action.RUN_COMMAND;

@Singleton
public class InteractableMessages extends Feature {

	private static final String TP_ACCEPT = "Um die Anfrage anzunehmen, schreibe /tpaccept.";
	private static final String TP_DENY = "Um sie abzulehnen, schreibe /tpdeny.";
	private static final Pattern P_H_PATTERN = Pattern.compile("^.*(?<command>/p h [^ ]+).*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern CLAN_INFO_PATTERN = Pattern.compile("^» (?<name>[^ ]+) \\((?:offline|online)\\)$");
	private static final Pattern PLOT_INFO_PLAYER_PATTERN = Pattern.compile("^§r§7(?:Besitzer|Helfer|Vertraut|Verboten): .+$");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Interagierbare Nachrichten")
		.description("""
			Macht Folgendes interagierbar:
			- TPAs (§a/tpaccept§r und §c/tpdeny§r)
			- Den Citybuild bei Globalchat-Nachrichten (Switcht zum CB)
			- Den Clan-Namen bei Globalchat-Nachrichten (Führt /clan info aus)
			- Den Status, Msgs, Plotchat- und Globalchat-Nachrichten (Schlägt /msg vor)
			- "/p h" in Nachrichten (Teleportiert zum Plot)
			- Spielernamen bei /clan info (Öffnet das Profil)
			- Spielernamen bei /p i (Öffnet das Profil)""")
		.icon("XZRF:mouse_left");

	@EventListener(priority = Priority.LOW)
	public void modifyMessage(MessageModifyEvent event) {
		modifyGlobalChats(event);
		modifyTps(event);
		modifyPHs(event);
		modifyClanInfo(event);
		modifyPlotInfo(event);
		addMsgSuggestions(event);
	}

	private void modifyGlobalChats(MessageModifyEvent event) {
		Matcher matcher = GLOBAL_CHAT_PATTERN.matcher(event.original.getFormattedText());
		if (!matcher.matches())
			return;

		String cb = matcher.group("cb");
		String clan = matcher.group("clantag");
		if (clan != null)
			clan = clan.replace('§', '&');

		IChatComponent message = event.message;

		boolean foundCb = false;
		boolean foundClan = false;

		for (IChatComponent sibling : message.getSiblings()) {
			if (sibling.getUnformattedTextForChat().equals(cb)) {
				sibling.getChatStyle()
					.setChatClickEvent(new ClickEvent(RUN_COMMAND, "/switch " + Citybuild.getCitybuild(cb).getInternalName()))
					.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§6Klicke, um auf den CB zu wechseln")));

				foundCb = true;
				if (clan == null)
					break;
			}

			if (foundCb) {
				if (!foundClan) {
					if (sibling.getFormattedText().equals("§6[§r"))
						foundClan = true;
					continue;
				}

				if (sibling.getFormattedText().equals("§6] §r"))
					break;

				sibling.getChatStyle().setChatClickEvent(new ClickEvent(RUN_COMMAND, "/clan info " + clan));
			}
		}

		event.setMessage(message);
	}

	private void modifyTps(MessageModifyEvent event) {
		String msg = event.original.getUnformattedText();

		if (!msg.equals(TP_ACCEPT) && !msg.equals(TP_DENY))
			return;

		String command = msg.equals(TP_ACCEPT) ? "/tpaccept" : "/tpdeny";

		IChatComponent component = event.message;

		for (IChatComponent part : component.getSiblings())
			if (part.getUnformattedText().equals(command))
				part.getChatStyle().setChatClickEvent(new ClickEvent(RUN_COMMAND, command));

		event.setMessage(component);
	}

	private void modifyPHs(MessageModifyEvent event) {
		Matcher matcher = P_H_PATTERN.matcher(event.message.getUnformattedText());
		if (!matcher.matches())
			return;

		String command = matcher.group("command");
		String unformatted = event.message.getUnformattedText();

		int startIndex = unformatted.indexOf(command);
		int endIndex = startIndex + command.length();
		int i = 0;

		List<IChatComponent> components = new ArrayList<>();
		components.add(event.message);

		ListIterator<IChatComponent> it = components.listIterator();
		while (it.hasNext()) {
			IChatComponent t = it.next();
			t.getSiblings().forEach(it::add);
			t.getSiblings().forEach(s -> it.previous());
		}

		for (IChatComponent c : components) {
			if (!(c instanceof ChatComponentText))
				return;

			ChatStyle style = c.getChatStyle().createDeepCopy();

			String text = c.getUnformattedTextForChat();
			int len = text.length();
			if (i >= startIndex && i + len <= endIndex) { // The entire component is part of the command
				c.getChatStyle().setChatClickEvent(new ClickEvent(RUN_COMMAND, command));
			} else if (i <= startIndex && len + i > startIndex) { // Command is at the end
				Reflection.set(c, "text", text.substring(0, startIndex - i));
				IChatComponent commandComponent = new ChatComponentText(text.substring(startIndex - i));
				commandComponent.getChatStyle().setChatClickEvent(new ClickEvent(RUN_COMMAND, command));
				commandComponent.getChatStyle().setParentStyle(c.getChatStyle());
				c.getSiblings().add(0, commandComponent);
				c = commandComponent;
				text = commandComponent.getUnformattedTextForChat();
				len = text.length();
				i = startIndex;
			}
			if (i + len >= endIndex) {
				int index = i >= startIndex ? endIndex : startIndex;
				if (i + len > index) { // Command is at the start
					Reflection.set(c, "text", text.substring(0, index - i));
					IChatComponent postComponent = new ChatComponentText(text.substring(index - i));
					postComponent.setChatStyle(style);
					c.getChatStyle().setChatClickEvent(new ClickEvent(RUN_COMMAND, command));
					c.getSiblings().add(0, postComponent);
				}
				break;
			}
			i += len;
		}
	}

	private void modifyClanInfo(MessageModifyEvent event) {
		String unformattedText = event.original.getUnformattedText();
		Matcher matcher = CLAN_INFO_PATTERN.matcher(unformattedText);
		if (!matcher.matches())
			return;

		String name = matcher.group("name");
		IChatComponent message = event.message;

		for (IChatComponent part : message.getSiblings())
			part.getChatStyle().setChatClickEvent(new ClickEvent(RUN_COMMAND, String.format("/profil %s ", name)));

		event.setMessage(message);
	}

	private void modifyPlotInfo(MessageModifyEvent event) {
		if (event.original.getFormattedText().startsWith("§r§7ID: ")) {
			for (IChatComponent sibling : event.message.getSiblings()) {
				if (!sibling.getUnformattedText().endsWith(": "))
					sibling.getChatStyle().setChatClickEvent(new ClickEvent(RUN_COMMAND, "/p h " + sibling.getUnformattedText()));
			}
			return;
		}

		if (!PLOT_INFO_PLAYER_PATTERN.matcher(event.original.getFormattedText()).matches())
			return;

		List<IChatComponent> siblings = event.message.getSiblings();

		boolean modify = false;
		for (IChatComponent sibling : siblings) {
			if (sibling.getUnformattedText().endsWith(": "))
				modify = true;
			else if (modify) {
				if (sibling.getChatStyle().getColor() != EnumChatFormatting.YELLOW) // red = invalid, gray = empty
					continue;

				if (!sibling.getUnformattedText().trim().isEmpty())
					sibling.getChatStyle().setChatClickEvent(new ClickEvent(RUN_COMMAND, "/profil " + sibling.getUnformattedText()));
			}
		}
	}

	public void addMsgSuggestions(MessageModifyEvent event) {
		String text = event.original.getFormattedText();

		for (Pattern p : new Pattern[] {PLOTCHAT_RECEIVE_PATTERN, MESSAGE_RECEIVE_PATTERN, MESSAGE_SEND_PATTERN, STATUS_PATTERN, GLOBAL_CHAT_PATTERN}) {
			Matcher matcher = p.matcher(text);
			if (!matcher.find())
				continue;

			String name = matcher.group("name").replaceAll("§.", "");
			event.message.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/msg %s ", name)));
			return;
		}
	}

}
