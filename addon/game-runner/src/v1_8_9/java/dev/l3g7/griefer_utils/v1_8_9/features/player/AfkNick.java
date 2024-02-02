/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.player;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Constants;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.types.*;
import dev.l3g7.griefer_utils.v1_8_9.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.InputEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.TickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.v1_8_9.misc.NameCache;
import dev.l3g7.griefer_utils.laby4.util.Laby4Util;
import net.minecraft.init.Items;

import java.util.regex.Matcher;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.*;

@Singleton
public class AfkNick extends Feature {

	private long lastEvent = 0;
	private boolean manuallyAFK = false;
	private boolean isAFK = false;

	private final StringSetting nickName = StringSetting.create()
		.name("Nick")
		.description("Wie du genickt werden willst, wenn du AFK bist." +
			"\n%name% wird mit deinem Namen ersetzt.")
		.defaultValue("AFK_%name%")
		.icon(Items.writable_book);

	private final KeySetting triggerAfk = KeySetting.create()
		.name("Hotkey")
		.icon("key")
		.description("Markiert dich automatisch als AFK, wenn diese Taste gedrück wird.")
		.pressCallback(b -> {
			if (!b)
				return;

			isAFK = manuallyAFK = true;
			lastEvent = 0;
			send("/nick " + nickName.get().replace("%name%", name()));
		});

	private final NumberSetting minutes = NumberSetting.create()
		.name("Minuten")
		.description("Nach wie vielen Minuten du als AFK eingestuft werden sollst.")
		.defaultValue(5)
		.icon(Items.clock);

	private final NumberSetting seconds = NumberSetting.create()
		.name("Sekunden")
		.description("Nach wie vielen Sekunden du als AFK eingestuft werden sollst.")
		.icon(Items.clock);

	private final StringSetting messageReplay = StringSetting.create()
		.name("Nachricht-\nbeantworter")
		.description("Mit welcher Nachricht geantwortet wird, wenn dir jemand eine /msg schreibt, während du AFK bist."
			+ "\n(Leerlassen zum deaktivieren)")
		.icon(Items.writable_book);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Automatisch nicken wenn AFK")
		.description("Nickt dich, wenn du eine bestimmte, einstellbare Zeit AFK bist.")
		.icon("labymod_3/afk_timer")
		.subSettings(nickName, messageReplay, triggerAfk, HeaderSetting.create(), minutes, seconds);

	@EventListener(triggerWhenDisabled = true)
	private void onKeyboardInput(InputEvent.KeyInputEvent event) {
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
		if (!isAFK || messageReplay.get().isEmpty())
			return;

		Matcher matcher = Constants.MESSAGE_RECEIVE_PATTERN.matcher(event.message.getFormattedText());
		if (!matcher.matches())
			return;

		String name = NameCache.ensureRealName(matcher.group("name").replaceAll("§.", ""));
		if (name.equals(name()))
			return;

		send("/msg " + name + " " + messageReplay.get());
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
		if (Laby4Util.isSettingOpened(enabled))
			return;

		long diff = System.currentTimeMillis() - lastEvent;

		if (diff > (minutes.get() * 60 + seconds.get()) * 1000) {
			if (isAFK)
				return;

			isAFK = true;
			send("/nick " + nickName.get().replace("%name%", name()));
			return;
		}

		if (!isAFK)
			return;

		isAFK = manuallyAFK = false;
		send("/unnick");
	}

}
