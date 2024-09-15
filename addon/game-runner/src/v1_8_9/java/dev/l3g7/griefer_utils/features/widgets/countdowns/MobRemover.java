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
import dev.l3g7.griefer_utils.core.api.misc.functions.Supplier;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.core.misc.TPSCountdown;
import dev.l3g7.griefer_utils.core.misc.server.GUClient;
import dev.l3g7.griefer_utils.core.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.widgets.Widget.SimpleWidget;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class MobRemover extends SimpleWidget {

	private static final Pattern MOB_REMOVER_PATTERN = Pattern.compile("§r§8\\[§r§6MobRemover§r§8] §r§4Achtung! §r§7In §r§e(?<minutes>\\d) Minuten? §r§7werden alle Tiere gelöscht\\.§r");

	private final DropDownSetting<TimeFormat> timeFormat = DropDownSetting.create(TimeFormat.class)
		.name("Zeitformat")
		.description("In welchem Format die verbleibende Zeit angezeigt werden soll.")
		.icon("hourglass")
		.defaultValue(TimeFormat.LONG);

	private final NumberSetting warnTime = NumberSetting.create()
		.name("Warn-Zeit (s)")
		.description("Wie viele Sekunden vor dem nächsten MobRemover eine Warnung angezeigt werden soll.")
		.icon("labymod_3/exclamation_mark");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("MobRemover")
		.description("Zeigt dir die Zeit bis zum nächsten MobRemover an.")
		.icon("skull_crossed_out")
		.subSettings(timeFormat, warnTime);

	private TPSCountdown countdown = null;

	@Override
	public String getValue() {
		if (countdown == null || countdown.isExpired())
			return "Unbekannt";

		// Warn if mob remover is less than the set amount of seconds away
		countdown.checkWarning("MobRemover!", warnTime.get());
		return Util.formatTimeSeconds(countdown.secondsRemaining(), timeFormat.get() == TimeFormat.SHORT);
	}

	@EventListener(triggerWhenDisabled = true)
	public void onServerSwitch(ServerSwitchEvent p) {
		countdown = null;
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMessageReceive(MessageReceiveEvent event) {
		Matcher matcher = MOB_REMOVER_PATTERN.matcher(event.message.getFormattedText());
		if (matcher.matches())
			countdown = TPSCountdown.fromMinutes(Integer.parseInt(matcher.group("minutes")));
		else if (event.message.getFormattedText().matches("^§r§8\\[§r§6MobRemover§r§8] §r§7Es wurden (?:§r§\\d+§r§7|keine) Tiere entfernt\\.§r$"))
			countdown = TPSCountdown.fromMinutes(15);
		else
			return;

		if (GUClient.get().isAvailable()) {
			new Thread(() -> {
				long passedSeconds = System.currentTimeMillis() / 1000;
				GUClient.get().sendMobRemoverData(MinecraftUtil.getCurrentCitybuild(), countdown.secondsRemaining() + passedSeconds);
			}).start();
		}
	}

	@EventListener(triggerWhenDisabled = true)
	private void onCitybuildEarlyJoin(CitybuildJoinEvent.Early event) {
		if (!GUClient.get().isAvailable())
			return;

		CompletableFuture.supplyAsync((Supplier<Long>) () -> GUClient.get().getMobRemoverData(event.citybuild)).thenAccept(end -> {
			if (end != null)
				countdown = TPSCountdown.fromEnd(end);
		});
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
