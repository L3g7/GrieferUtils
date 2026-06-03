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
import dev.l3g7.griefer_utils.core.api.misc.Option;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.events.network.TabListEvent;
import dev.l3g7.griefer_utils.core.misc.NameCache;
import dev.l3g7.griefer_utils.core.misc.color_patterns.ColorPattern;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.IChatComponentUtil;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.core.util.PlayerUtil;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.misc.Constants.FORMATTED_PLAYER_PATTERN;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

@Singleton
public class DefaultPrefixes extends Feature {

	private static class DefaultPattern extends ColorPattern {
		private final boolean boldInTabList;

		public DefaultPattern(char color, boolean boldInChat, boolean boldInTabList) {
			super(color, boldInChat);
			this.boldInTabList = boldInTabList;
		}

		public DefaultPattern(char color, boolean bold) {
			this(color, bold, bold);
		}
	}

	private static final DefaultPattern FALLBACK = new DefaultPattern('4', false);

	private static final Map<String, DefaultPattern> DEFAULT_COLORS = new HashMap<>() {{
		put("Owner", new DefaultPattern('4', true));
		put("Administrator", new DefaultPattern('4', true));
		put("Organisator", new DefaultPattern('c', true));
		put("ShopManager", new DefaultPattern('c', false));
		put("Developer", new DefaultPattern('b', false));
		put("Content", new DefaultPattern('e', false));
		put("Moderator", new DefaultPattern('2', false));
		put("Supporter", new DefaultPattern('2', false));
		put("Designer", new DefaultPattern('9', false));
		put("Builder", new DefaultPattern('3', false));
		put("Freund", new DefaultPattern('c', false));

		put("Streamer+", new DefaultPattern('5', false));
		put("Streamer", new DefaultPattern('5', false));
		put("YouTuber+", new DefaultPattern('5', false));
		put("YouTuber", new DefaultPattern('5', false));
		put("Helfer", new DefaultPattern('2', false));

		put("Hero", new DefaultPattern('e', true, true));
		put("Supreme", new DefaultPattern('d', true, false));
		put("Griefer", new DefaultPattern('4', true, false));
		put("Titan", new DefaultPattern('9', false));
		put("Legende", new DefaultPattern('c', false));
		put("Ultra", new DefaultPattern('b', false));
		put("Premium", new DefaultPattern('6', false));
		put("Spieler", new DefaultPattern('7', false));
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
		.icon("name_tag_rainbow")
		.defaultValue(true)
		.callback(TabListEvent::updatePlayerInfoList);

	private final SwitchSetting removePrefixNames = SwitchSetting.create()
		.name("Benutzerdefinierte Namen entfernen")
		.description("Ob die Rang-Namen auf die Standardnamen zurückgesetzt werden sollen.")
		.icon("name_tag_yellow")
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
		.name(LABY_3.isActive()
			? "Standard- Nametags" // Spacing to allow word wrap
			: "Standard-Nametags")
		.description("Entfernt Änderungen an Nametags.")
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

		modify(event.component, FORMATTED_PLAYER_PATTERN, false);
	}

	@EventListener(priority = Priority.HIGH)
	public void onMessageModifyChat(MessageEvent.MessageModifyEvent event) {
		if (!(removeColors.get() || removePrefixNames.get() || removeSuffixes.get()))
			return;

		if (!chat.get() || !event.message.getUnformattedText().contains("┃"))
			return;

		for (Pattern pattern : Constants.MESSAGE_PATTERNS)
			modify(event.message, pattern, true);
	}

	private void modify(IChatComponent message, Pattern pattern, boolean isChat) {
		if (removePrefixNames.get()) {
			IChatComponentUtil.replace(message, pattern, "rank", m -> {
				String name = m.group("name").replaceAll("§.", "");
				if (shouldSkipSelf(name))
					return null;

				Option<String> realRank = PlayerUtil.getRank(name);
				if (realRank.isUnset())
					return null;

				ColorPattern color = ColorPattern.from(m);
				return color.paintRank(realRank.get(), isChat || isBoldInTabList(m), new ChatStyle());
			});
		}

		if (removeColors.get()) {
			IChatComponentUtil.replace(message, pattern, "name", m -> buildDefaultColorsTag(m, true, isChat));
			IChatComponentUtil.replace(message, pattern, "rank", m -> buildDefaultColorsTag(m, false, isChat));
		}

		if (removeSuffixes.get()) {
			IChatComponentUtil.replace(message, pattern, "suffix", m -> {
				System.out.println("Removing " + m);
				return Collections.emptyList();
			});
		}
	}

	private List<IChatComponent> buildDefaultColorsTag(Matcher m, boolean isName, boolean isChat) {
		String name = m.group("name").replaceAll("§.", "");
		String rank = m.group("rank").replaceAll("§.", "");
		if (shouldSkipSelf(name))
			return null;

		String realRank = PlayerUtil.getRank(name).getOr(rank);
		DefaultPattern color = DEFAULT_COLORS.get(realRank.startsWith("Sr") ? realRank.substring(2) : realRank);
		if (color == null)
			return null;

		if (isName)
			return color.paintName(name, color.boldInTabList || isChat, new ChatStyle());
		else
			return color.paintRank(rank, color.boldInTabList || isChat, new ChatStyle());
	}

	public static boolean isBoldInTabList(Matcher m) {
		String name = m.group("name").replaceAll("§.", "");
		String rank = m.group("rank").replaceAll("§.", "");
		String realRank = PlayerUtil.getRank(name).getOr(rank);

		ColorPattern pattern = ColorPattern.from(m);
		DefaultPattern defaultColors = DEFAULT_COLORS.getOrDefault(realRank, FALLBACK);
		return defaultColors.boldInTabList && defaultColors.equals(pattern); // Only allow bold if using default prefix
	}

	private boolean shouldSkipSelf(String fmtName) {
		String name = fmtName.replaceAll("§.", "");
		return !self.get() && player() != null && MinecraftUtil.name().equals(NameCache.ensureRealName(name));
	}

}
