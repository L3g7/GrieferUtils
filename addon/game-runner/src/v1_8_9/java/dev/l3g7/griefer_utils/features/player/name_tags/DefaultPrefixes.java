/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.player.name_tags;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.api.util.StringUtil;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.events.network.TabListEvent;
import dev.l3g7.griefer_utils.core.misc.NameCache;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.IChatComponentUtil;
import dev.l3g7.griefer_utils.core.util.IChatComponentUtil.MutableComponent;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.core.util.PlayerUtil;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.chat.ingoing.auto_unnick.PrefixFinder;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.util.IChatComponentUtil.paint;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

@Singleton
public class DefaultPrefixes extends Feature {

	private static final Map<String, String> DEFAULT_COLORS = new HashMap<>() {{
		put("Owner", "4");
		put("Administrator", "4");
		put("Organisator", "c");
		put("ShopManager", "c");
		put("Developer", "b");
		put("Content", "e");
		put("Moderator", "2");
		put("Supporter", "2");
		put("Designer", "9");
		put("Builder", "3");
		put("Freund", "c");

		put("Streamer+", "5");
		put("Streamer", "5");
		put("YouTuber+", "5");
		put("YouTuber", "5");
		put("Helfer", "2");

		put("Hero", "el");
		put("Supreme", "dl");
		put("Griefer", "4l");
		put("Titan", "9");
		put("Legende", "c");
		put("Ultra", "b");
		put("Premium", "6");
		put("Spieler", "7");
	}};

	private final SwitchSetting tab = SwitchSetting.create()
		.name("In Tabliste")
		.description("Ob die Prefixe in der Tabliste geändert werden sollen.")
		.icon("blackboard")
		.defaultValue(true)
		.callback(TabListEvent::updatePlayerInfoList);

	private final SwitchSetting chat = SwitchSetting.create()
		.name("Im Chat")
		.description("Ob die Prefixe im Chat geändert werden sollen.")
		.icon("chat")
		.defaultValue(true);

	private final SwitchSetting self = SwitchSetting.create()
		.name("Eigenen Prefix ändern")
		.description("Ob der eigene Prefix auch geändert werden soll.")
		.icon("steve")
		.defaultValue(true)
		.callback(TabListEvent::updatePlayerInfoList);

	private final SwitchSetting removeColors = SwitchSetting.create()
		.name("Benutzerdefinierte Farben entfernen")
		.description("Ob die Prefix-Farben auf die Standardfarben zurückgesetzt werden sollen.")
		.icon("tabping_colored")
		.defaultValue(true)
		.callback(TabListEvent::updatePlayerInfoList);

	private final SwitchSetting removePrefixNames = SwitchSetting.create()
		.name("Benutzerdefinierte Namen entfernen")
		.description("Ob die Rang-Namen auf die Standardnamen zurückgesetzt werden sollen.")
		.icon("name_tag")
		.defaultValue(true)
		.callback(TabListEvent::updatePlayerInfoList);

	private final SwitchSetting removeSuffixes = SwitchSetting.create()
		.name("Suffixe entfernen")
		.description("Ob die Suffixe entfernt werden sollen.")
		.icon("name_tag")
		.defaultValue(true)
		.callback(TabListEvent::updatePlayerInfoList);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Standard-Prefixe")
		.description("Setzt die Prefixe jeder Person auf den standard Prefix des jeweiligen Ranges.")
		.icon("name_tag_yellow")
		.callback(TabListEvent::updatePlayerInfoList)
		.subSettings(chat, tab, self, HeaderSetting.create(), removeColors, removePrefixNames, removeSuffixes);

	@Override
	public void init() {
		super.init();
		getCategory().callback(TabListEvent::updatePlayerInfoList);
	}

	@EventListener(priority = Priority.HIGHEST)
	public void onTabListNameUpdate(TabListEvent.TabListNameUpdateEvent event) {
		if (!(removeColors.get() || removePrefixNames.get() || removeSuffixes.get()))
			return;

		if (!tab.get() || !event.component.getUnformattedText().contains("┃"))
			return;

		String unformatted = event.component.getUnformattedText();
		String[] parts = unformatted.split(" ┃ ");
		if (parts.length != 2)
			return;

		String fmtName = parts[1].trim();
		String fmtRank = parts[0].trim();
		if (shouldSkipSelf(fmtName))
			return;

		// remove extra data (e.g. [LIVE] ) from name
		fmtName = fmtName.split(" ")[0];

		setPrefix(event.component, fmtName, fmtRank, true);
		removeSuffix(event.component);
	}

	@EventListener(priority = Priority.HIGH)
	public void onMessageModifyChat(MessageEvent.MessageModifyEvent event) {
		if (!(removeColors.get() || removePrefixNames.get() || removeSuffixes.get()))
			return;

		if (!chat.get() || !event.message.getUnformattedText().contains("┃"))
			return;

		for (Pattern pattern : Constants.MESSAGE_PATTERNS) {
			Matcher matcher = pattern.matcher(event.message.getFormattedText());

			if (matcher.matches()) {
				String fmtName = matcher.group("name").trim();
				String fmtRank = matcher.group("rank").trim();
				if (shouldSkipSelf(fmtName))
					return;

				setPrefix(event.message, fmtName, fmtRank, false);
				removeSuffix(event.message);
				return;
			}
		}
	}

	private boolean shouldSkipSelf(String fmtName) {
		String name = fmtName.replaceAll("§.", "");
		return !self.get() && player() != null && MinecraftUtil.name().equals(NameCache.ensureRealName(name));
	}

	private void setPrefix(IChatComponent component, String fmtName, String fmtRank, boolean isTabList) {
		String name = fmtName.replaceAll("§.", "");
		String rank = fmtRank.replaceAll("§.", "");

		String color = null;
		if (removeColors.get())
			color = DEFAULT_COLORS.get(rank.startsWith("Sr") ? rank.substring(2) : rank);
		if (color == null)
			color = PrefixFinder.getPrefix(fmtRank, fmtName);

		if (removePrefixNames.get()) {
			String realRank = PlayerUtil.getRank(name);
			if (!realRank.isEmpty())
				rank = realRank;
		}

		IChatComponentUtil.setNameWithPrefix(component, name, name, color, isTabList);
		setRankWithPrefix(component, rank, color, isTabList);
	}

	private void removeSuffix(IChatComponent root) {
		if (!removeSuffixes.get() || StringUtil.count(root.getUnformattedText().trim(), ' ') < 3)
			return;

		List<MutableComponent> components = IChatComponentUtil.getNestedSiblings(root);

		// Find delimiter
		int spaces = 0;
		for (ListIterator<MutableComponent> iterator = components.listIterator(); iterator.hasNext(); ) {
			MutableComponent component = iterator.next();
			int cSpaces = StringUtil.count(component.getText(), ' ');
			spaces += cSpaces;

			if (spaces >= 2) {
				// Reached start of suffix
				String text = component.getText();

				// Remove whitespace, potential suffix
				component.setText(text.substring(0, text.lastIndexOf(' ')));

				if (text.endsWith(" ")) {
					// Suffix is in next component
					iterator.next().remove();
				}

				return;
			}
		}
	}

	private void setRankWithPrefix(IChatComponent iChatComponent, String rank, String prefix, boolean isTabList) {
		List<IChatComponent> everything = iChatComponent.getSiblings();
		if (everything.isEmpty()) {
			// TODO: wtf
			return;
		}

		IChatComponent parent = everything.get(everything.size() - 1);

		if (parent.getSiblings().isEmpty())
			parent = iChatComponent;

		List<IChatComponent> lastSiblings = parent.getSiblings();

		int startIndex = 0;
		boolean reachedSeparator = false;

		for (ListIterator<IChatComponent> iterator = lastSiblings.listIterator(); reachedSeparator ? iterator.hasPrevious() : iterator.hasNext(); ) {
			String text = (reachedSeparator ? iterator.previous() : iterator.next()).getUnformattedTextForChat();

			if (!reachedSeparator) {
				if (text.contains("\u2503")) {
					reachedSeparator = true;

					// The json is pretty wierd
					iterator.previous();
					iterator.previous();
					startIndex = iterator.previousIndex();
					iterator.remove();
				}
				continue;
			}

			if (text.contains(" ") || text.contains("[") || text.contains("]")) {
				startIndex++;
				break;
			} else {
				startIndex = iterator.previousIndex();
				iterator.remove();
			}
		}

		Collection<IChatComponent> nameComponents = new ArrayList<>(paint(rank, prefix, isTabList));

		// Add the HoverEvent and make it italic
		ClickEvent clickEvent = parent.getChatStyle().getChatClickEvent();

		for (IChatComponent component : nameComponents) {

			if (clickEvent != null)
				component.getChatStyle().setChatClickEvent(clickEvent);
		}

		nameComponents.add(new ChatComponentText(" "));

		parent.getChatStyle().setChatClickEvent(clickEvent);
		lastSiblings.addAll(Math.max(startIndex, 0), nameComponents);
	}

}
