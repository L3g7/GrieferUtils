/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageModifyEvent;
import dev.l3g7.griefer_utils.core.misc.IconComponent;
import dev.l3g7.griefer_utils.core.misc.NameCache;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.*;

@Singleton
public class MessageSkulls extends Feature {

	private static final ArrayList<Pattern> PATTERNS = new ArrayList<>(MESSAGE_PATTERNS) {{
		remove(GLOBAL_CHAT_PATTERN);
		add(STATUS_PATTERN);
	}};

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Kopf vor Nachrichten")
		.description("Zeigt den Kopf des Autors vor Nachrichten an.")
		.icon("steve");

	@EventListener
	public void onMsgReceive(MessageModifyEvent event) {
		for (Pattern pattern : PATTERNS) {
			Matcher matcher = pattern.matcher(event.original.getFormattedText());
			if (!matcher.matches())
				continue;

			String name = NameCache.ensureRealName(matcher.group("name").replaceAll("§.", ""));

			event.setMessage(new ChatComponentText("")
				.appendSibling(IconComponent.head(name))
				.appendText(" ")
				.appendSibling(event.message));
			return;
		}

	}

}
