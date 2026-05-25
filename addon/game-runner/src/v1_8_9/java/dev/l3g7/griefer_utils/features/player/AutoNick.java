/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * This file has been modified by itzW0lf.
 */

package dev.l3g7.griefer_utils.features.player;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.core.events.InputEvent.KeyInputEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.TickEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.core.misc.NameCache;
import dev.l3g7.griefer_utils.core.settings.types.*;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.labymod.laby4.util.Laby4Util;
import net.labymod.settings.LabyModAddonsGui;

import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;
import static dev.l3g7.griefer_utils.labymod.laby3.util.AddonsGuiWithCustomBackButton.path;

@Singleton
public class AutoNick extends Feature {

	private long lastEvent = 0;
	private boolean manuallyAFK = false;
	private boolean isAFK = false;
	private boolean wasNicked = false;

	public static boolean isAfk() {
		return get(AutoNick.class).isAFK;
	}

	private final StringSetting nickName = StringSetting.create()
		.name("Nick")
		.description("Wie du genickt werden willst, wenn du AFK bist.\n%name% wird mit deinem Namen ersetzt.")
		.defaultValue("AFK_%name%")
		.icon("name_tag");

	private final SwitchSetting autoNick = SwitchSetting.create()
		.name("Automatisch nicken")
		.description("Nickt dich automatisch mit dem eingestellten Nick, wenn du AFK bist.")
		.icon("name_tag")
		.since("2.5.0")
		.defaultValue(true)
		.subSettings(nickName);

	private final StringSetting messageReply = StringSetting.create()
		.name("Nachricht")
		.description("Mit welcher Nachricht geantwortet wird, wenn dir jemand eine /msg schreibt, während du AFK bist.")
		.icon("book_and_quill");

	private final SwitchSetting autoMessage = SwitchSetting.create()
		.name("Automatische Antwort")
		.description("Antwortet automatisch auf /msg-Nachrichten, wenn du AFK bist.")
		.icon("book_and_quill")
		.since("2.5.0")
		.subSettings(messageReply);

	private final KeySetting triggerAfk = KeySetting.create()
		.name("Hotkey")
		.icon("key")
		.since("2.5.0")
		.description("Markiert dich automatisch als AFK, wenn diese Taste gedrückt wird.")
		.pressCallback(b -> {
			if (!b)
				return;

			isAFK = manuallyAFK = true;
			lastEvent = 0;
			if (autoNick.get()) {
				send("/nick " + nickName.get().replace("%name%", MinecraftUtil.name()));
				wasNicked = true;
			}
		});

	private final NumberSetting minutes = NumberSetting.create()
		.name("Minuten")
		.description("Nach wie vielen Minuten du als AFK eingestuft werden sollst.")
		.defaultValue(5)
		.icon("clock");

	private final NumberSetting seconds = NumberSetting.create()
		.name("Sekunden")
		.description("Nach wie vielen Sekunden du als AFK eingestuft werden sollst.")
		.icon("clock");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Automatisch AFK erkennen")
		.description("Erkennt, wenn du eine bestimmte Zeit AFK bist, und führt die aktivierten Aktionen aus.")
		.icon("afk_timer")
		.subSettings(autoNick, autoMessage, triggerAfk, HeaderSetting.create(), minutes, seconds);

	@EventListener(triggerWhenDisabled = true)
	private void onKeyboardInput(KeyInputEvent event) {
		if (!manuallyAFK)
			lastEvent = System.currentTimeMillis();
	}

	@EventListener(triggerWhenDisabled = true)
	private void onServerSwitch(ServerSwitchEvent event) {
		if (!manuallyAFK)
			lastEvent = System.currentTimeMillis();
	}

	@EventListener(triggerWhenDisabled = true)
	private void onGuiKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (!manuallyAFK)
			lastEvent = System.currentTimeMillis();
	}

	@EventListener
	private void onMsg(MessageReceiveEvent event) {
		if (!isAFK || !autoMessage.get() || messageReply.get().isEmpty())
			return;

		Matcher matcher = Constants.MESSAGE_RECEIVE_PATTERN.matcher(event.message.getFormattedText());
		if (!matcher.matches())
			return;

		String nick = matcher.group("name");
		String realName = NameCache.ensureRealName(nick);

		if (realName != null && realName.replaceAll("§.", "").equals(MinecraftUtil.name()))
			return;

		send("/msg " + (realName == null ? nick : realName).replaceAll("§.", "") + " " + messageReply.get());
	}

	@EventListener
	private void onTick(TickEvent.ClientTickEvent event) {
		if (player() == null)
			return;

		if (player().motionX > 0 || player().motionY > 0 || player().motionZ > 0) {
			lastEvent = System.currentTimeMillis();
			return;
		}

		if (lastEvent == 0 || (minutes.get() == 0 && seconds.get() == 0))
			return;

		// Check settings are currently being edited
		if (LABY_4.isActive()) {
			if (Laby4Util.isSettingOpened(enabled))
				return;
		} else {
			// TODO cleanup + fix import of path()
			if (mc().currentScreen instanceof LabyModAddonsGui && !path().isEmpty() && path().get(path().size() - 1) == enabled)
				return;
		}

		long diff = System.currentTimeMillis() - lastEvent;

		if (diff > (minutes.get() * 60 + seconds.get()) * 1000) {
			if (isAFK)
				return;

			isAFK = true;
			if (autoNick.get()) {
				send("/nick " + nickName.get().replace("%name%", MinecraftUtil.name()));
				wasNicked = true;
			}
			return;
		}

		if (!isAFK)
			return;

		isAFK = manuallyAFK = false;
		if (wasNicked) {
			send("/unnick");
			wasNicked = false;
		}
	}

}
