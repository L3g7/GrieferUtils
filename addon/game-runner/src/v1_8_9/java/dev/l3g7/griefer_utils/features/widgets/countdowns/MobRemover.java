/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.countdowns;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.events.network.GrieferGamesPayloadEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.core.misc.Countdown;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.widgets.Widget.SimpleWidget;

import java.io.DataInputStream;
import java.io.IOException;

import static dev.l3g7.griefer_utils.core.api.util.Util.elevate;

@Singleton
public class MobRemover extends SimpleWidget {

	private final DropDownSetting<TimeFormat> timeFormat = DropDownSetting.create(TimeFormat.class)
		.name("Zeitformat")
		.description("In welchem Format die verbleibende Zeit angezeigt werden soll.")
		.icon("hourglass")
		.defaultValue(TimeFormat.LONG);

	private final NumberSetting warnTime = NumberSetting.create()
		.name("Warn-Zeit (s)")
		.description("Wie viele Sekunden vor dem nächsten MobRemover eine Warnung angezeigt werden soll.")
		.icon("clock");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("MobRemover")
		.description("Zeigt dir die Zeit bis zum nächsten MobRemover an.")
		.icon("crossed_out_zombie")
		.subSettings(timeFormat, warnTime);

	private final Countdown countdown = Countdown.ticking();

	@Override
	public String getValue() {
		if (countdown.isExpired())
			return "Unbekannt";

		// Warn if mob remover is less than the set amount of seconds away
		countdown.checkWarning("MobRemover!", warnTime.get());
		return Util.formatTimeSeconds(countdown.secondsRemaining(), timeFormat.get() == TimeFormat.SHORT);
	}

	@EventListener(triggerWhenDisabled = true)
	public void onServerSwitch(ServerSwitchEvent p) {
		countdown.invalidate();
	}

	@EventListener(triggerWhenDisabled = true)
	private void onMobRemover(GrieferGamesPayloadEvent event) {
		if (!event.channel.equals("entityremover"))
			return;

		try (DataInputStream in = event.createStream()) {
			this.countdown.set((int) in.readLong());
		} catch (IOException e) {
			throw elevate(e);
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
