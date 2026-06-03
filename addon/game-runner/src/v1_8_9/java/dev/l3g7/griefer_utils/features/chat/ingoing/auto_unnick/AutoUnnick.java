/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.ingoing.auto_unnick;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.events.network.TabListEvent;
import dev.l3g7.griefer_utils.core.misc.NameCache;
import dev.l3g7.griefer_utils.core.misc.color_patterns.ColorPattern;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.IChatComponentUtil;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.player.name_tags.DefaultPrefixes;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.*;

@Singleton
public class AutoUnnick extends Feature {

	private static final ArrayList<Pattern> PATTERNS = new ArrayList<>(MESSAGE_PATTERNS) {{
		// TODO move to Constants
		remove(GLOBAL_CHAT_PATTERN);
		add(STATUS_PATTERN);
	}};

	private final SwitchSetting tab = SwitchSetting.create()
		.name("In Tabliste")
		.description("Ob Spieler in der Tabliste entnickt werden sollen.")
		.icon("blackboard")
		.callback(TabListEvent::updatePlayerInfoList)
		.defaultValue(true);

	private final SwitchSetting chat = SwitchSetting.create()
		.name("In Chat")
		.description("Ob Spieler im Chat entnickt werden sollen.")
		.icon("chat")
		.defaultValue(true);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Automatisch entnicken")
		.description("Zeigt statt Nicks den echten Namen an.")
		.icon("name_tag")
		.callback(TabListEvent::updatePlayerInfoList)
		.subSettings(chat, tab);

	@Override
	public void init() {
		super.init();
		getCategory().callback(TabListEvent::updatePlayerInfoList);
	}

	@EventListener(priority = Priority.HIGH)
	public void onTabListNameUpdate(TabListEvent.TabListNameUpdateEvent event) {
		if (!tab.get())
			return;

		String text = event.component.getUnformattedText();
		if (!text.contains("~"))
			return;

		IChatComponentUtil.replace(event.component, FORMATTED_PLAYER_PATTERN, "name", m -> buildUnnickedTag(m, false, false));
	}

	@EventListener
	public void onMessageModifyChat(MessageEvent.MessageModifyEvent event) {
		if (!chat.get())
			return;

		String text = event.message.getUnformattedText();
		if (!text.contains("┃ ~") || text.startsWith("@"))
			return;

		for (Pattern pattern : PATTERNS)
			IChatComponentUtil.replace(event.message, pattern, "name", m -> buildUnnickedTag(m, true, true));
	}

	private static List<IChatComponent> buildUnnickedTag(Matcher m, boolean isChat, boolean addHover) {
		String nickName = m.group("name").replaceAll("§.", "");
		if (!nickName.contains("~"))
			return null;

		ColorPattern pattern = ColorPattern.from(m);
		String realName = NameCache.getName(nickName);
		if (realName == null)
			return null;

		boolean allowBold = isChat || DefaultPrefixes.isBoldInTabList(m);
		return pattern.paintName(realName, allowBold, new ChatStyle()
			.setChatHoverEvent(addHover
				? new HoverEvent(HoverEvent.Action.SHOW_TEXT, pattern.paintName(nickName, true))
				: null)
			.setItalic(true));
	}

}
