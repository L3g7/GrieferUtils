/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.countdowns;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.core.misc.Countdown;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.widgets.Widget.SimpleWidget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class MagicForestTimer extends SimpleWidget {

	private static final Pattern TIME_PATTERN = Pattern.compile("^§r§8\\[§r§6GrieferGames§r§8] §r§7Du hast noch §r§e(?<hours>\\d+) §r§7Stunden? §r§e(?<minutes>\\d+) §r§7Minuten? §r§e(?<seconds>\\d+) §r§7Sekunden? Zeit im Zauberwald zu sein\\.§r$");
	private static boolean waitingForTime = false;

	private static final Countdown COUNTDOWN = Countdown.realtime();

	private final DropDownSetting<TimeFormat> timeFormat = DropDownSetting.create(TimeFormat.class)
		.name("Zeitformat")
		.description("In welchem Format die verbleibende Zeit angezeigt werden soll.")
		.icon("hourglass")
		.defaultValue(TimeFormat.SHORT);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Zauberwald-\nTimer")
		.description("Zeigt dir an, wie viel Zeit du noch im Zauberwald hast.")
		.icon("earth_hourglass")
		.since("2.4-BETA-1")
		.subSettings(timeFormat);

	@Override
	public boolean isVisibleInGame() {
		return MinecraftUtil.getCurrentCitybuild() == Citybuild.MAGIC_FOREST;
	}

	@Override
	public String getValue() {
		int secondsRemaining = Math.max(COUNTDOWN.secondsRemaining(), 0);
		return Util.formatTimeSeconds(secondsRemaining, timeFormat.get() == TimeFormat.SHORT);
	}

	@EventListener
	private void onJoin(CitybuildJoinEvent.Early event) {
		if (event.citybuild != Citybuild.MAGIC_FOREST)
			return;

		MinecraftUtil.send("/zauberwald");
		waitingForTime = true;
	}

	@EventListener
	private void onMessageReceive(MessageReceiveEvent event) {
		if (!waitingForTime)
			return;

		Matcher matcher = TIME_PATTERN.matcher(event.message.getFormattedText());
		if (!matcher.matches())
			return;

		waitingForTime = false;
		int hours = Integer.parseInt(matcher.group("hours"));
		int minutes = Integer.parseInt(matcher.group("minutes"));
		int seconds = Integer.parseInt(matcher.group("seconds"));
		COUNTDOWN.set((hours * 60 + minutes) * 60 + seconds);
		event.cancel();
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
