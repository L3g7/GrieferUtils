/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.modules.timers;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Named;
import dev.l3g7.griefer_utils.api.misc.functions.Supplier;
import dev.l3g7.griefer_utils.api.util.Util;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.MessageEvent.MessageReceiveEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.Laby4Module;
import dev.l3g7.griefer_utils.v1_8_9.misc.server.GUClient;
import dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

@Singleton
public class MobRemover extends Laby4Module {

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

	private long mobRemoverEnd = -1;

	@Override
	public String getValue() {
		if (mobRemoverEnd == -1)
			return "Unbekannt";

		long diff = mobRemoverEnd - System.currentTimeMillis();
		if (diff < 0)
			return "Unbekannt";

		// Warn if mob remover is less than the set amount of seconds away
		if (diff < warnTime.get() * 1000) {
			String s = Util.formatTime(mobRemoverEnd, true);
			if (!s.equals("0s"))
				title("§c§l" + s);
		}

		return Util.formatTime(mobRemoverEnd, timeFormat.get() == TimeFormat.SHORT);
	}

	@EventListener(triggerWhenDisabled = true)
	public void onServerSwitch(ServerSwitchEvent p) {
		mobRemoverEnd = -1;
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMessageReceive(MessageReceiveEvent event) {
		Matcher matcher = MOB_REMOVER_PATTERN.matcher(event.message.getFormattedText());
		if(matcher.matches())
			mobRemoverEnd = System.currentTimeMillis() + Long.parseLong(matcher.group("minutes")) * 60L * 1000L;
		else if (event.message.getFormattedText().matches("^§r§8\\[§r§6MobRemover§r§8] §r§7Es wurden (?:§r§\\d+§r§7|keine) Tiere entfernt\\.§r$"))
			mobRemoverEnd = System.currentTimeMillis() + 15L * 60L * 1000L;
		else
			return;

		if (GUClient.get().isAvailable())
			new Thread(() -> GUClient.get().sendMobRemoverData(MinecraftUtil.getCurrentCitybuild(), mobRemoverEnd / 1000)).start();
	}

	@EventListener(triggerWhenDisabled = true)
	private void onCitybuildEarlyJoin(CitybuildJoinEvent.Early event) {
		if (!GUClient.get().isAvailable())
			return;

		CompletableFuture.supplyAsync((Supplier<Long>) () -> GUClient.get().getMobRemoverData(event.citybuild)).thenAccept(end -> {
			if (end != null)
				mobRemoverEnd = end * 1000;
		});
	}

	private void title(String title) {
		mc().ingameGUI.displayTitle("§cMobRemover!", null, -1, -1, -1);
		mc().ingameGUI.displayTitle(null, title, -1, -1, -1);
		mc().ingameGUI.displayTitle(null, null, 0, 2, 3);
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
