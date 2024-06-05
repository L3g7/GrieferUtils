/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.modules.laby4.timers;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageSendEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.modules.Laby4Module;
import net.minecraft.init.Items;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

@Singleton
@ExclusiveTo(LABY_4)
public class Chatlog extends Laby4Module {

	private boolean sentCmd = false;

	private final DropDownSetting<TimeFormat> timeFormat = DropDownSetting.create(TimeFormat.class)
		.name("Zeitformat")
		.description("In welchem Format die verbleibende Zeit angezeigt werden soll.")
		.icon("hourglass")
		.config("modules.chatlog.time_format")
		.defaultValue(TimeFormat.LONG);

	private final SwitchSetting hide = SwitchSetting.create()
		.name("Verstecken, wenn fertig")
		.description("Ob das Modul versteckt werden soll, wenn derzeit kein Cooldown existiert.")
		.icon("blindness")
		.config("modules.chatlog.hide");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Chatlog")
		.description("Zeigt dir den verbleibenden Cooldown bis zum nächsten /chatlog an.")
		.icon(Items.clock)
		.subSettings(timeFormat, hide);

	private long chatlogEnd = 0;

	@Override
	public Object getValue() {
		return Util.formatTime(chatlogEnd, timeFormat.get() == TimeFormat.SHORT);
	}

	@Override
	public boolean isVisibleInGame() {
		return !hide.get() || chatlogEnd >= System.currentTimeMillis();
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMessageSend(MessageSendEvent event) {
		String msg = event.message.toLowerCase();

		sentCmd = msg.startsWith("/chatlog") && !event.message.trim().equals("/chatlog");
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMessageReceive(MessageReceiveEvent event) {
		if (!sentCmd)
			return;

		String msg = event.message.getUnformattedText();

		if (msg.startsWith("[Chat-Log] Der Chat-Log wurde erfolgreich gespeichert: ") || msg.equals("------------ Chat-Log-Hilfe ------------")) {
			chatlogEnd = System.currentTimeMillis() + 30_000;
			sentCmd = false;
		}
	}

	private enum TimeFormat implements Named {
		SHORT("Kurz"),
		LONG("Lang");

		private final String name;
		TimeFormat(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

	}

}
