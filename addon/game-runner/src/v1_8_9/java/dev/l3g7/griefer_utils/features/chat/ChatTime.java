/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.init.Items;
import net.minecraft.util.ChatComponentText;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.IllegalFormatException;

import static dev.l3g7.griefer_utils.core.api.event_bus.Priority.LOW;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
public class ChatTime extends Feature {

	private final StringSetting style = StringSetting.create()
		.name("Design")
		.description("Das Design des Prefixes, mit Unterstützung von &-Formatierungscodes.\n" +
			"%s ist die Zeit an sich.")
		.icon("XZRF:color_palette")
		.defaultValue("&7[&6%s&7] ")
		.validator(v -> {
			try {
				String.format(v, "");
				return v.contains("%s");
			} catch (IllegalFormatException e) {
				return false;
			}
		});

	private final StringSetting format = StringSetting.create()
		.name("Zeitformat")
		.description("Das Format der Zeit, gemäß Javas Date Format.")
		.icon("XZRF:book_and_quill")
		.defaultValue("HH:mm:ss")
		.validator(v -> {
			try {
				new SimpleDateFormat(v);
				return true;
			} catch (IllegalArgumentException e) {
				return false;
			}
		});

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("ChatTime")
		.description("Fügt den Zeitpunkt des Empfangens vor Chatnachrichten hinzu.")
		.icon("XZRF:clock")
		.subSettings(style, format);

	public ChatTime() {
		if(enabled.getStorage().value == null) { // If no value loaded, try loading from TebosBrime's addon
			File configFile = new File(mc().mcDataDir, "LabyMod/addons-1.8/config/ChatTime.json");
			if(configFile.exists()) {
				IOUtil.read(configFile).asJsonObject().ifPresent(obj -> {
					JsonObject cfg = obj.get("config").getAsJsonObject();
					if (cfg.has("chatData"))
						format.set(cfg.get("chatData").getAsString());
					if (cfg.has("chatData2"))
						style.defaultValue(cfg.get("chatData2").getAsString().replace("%time%", "%s"));
				});
			}
		}
	}

	@EventListener(priority = LOW)
	public void onMessageModifyChat(MessageEvent.MessageModifyEvent event) {
		String time = String.format(style.get(), new SimpleDateFormat(format.get()).format(new Date())).replace('&', '§') + "§r";
		event.setMessage(new ChatComponentText(time).appendSibling(event.message));
	}

}
