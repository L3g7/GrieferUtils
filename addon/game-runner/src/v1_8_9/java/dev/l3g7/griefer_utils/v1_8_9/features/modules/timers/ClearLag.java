/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.modules.timers;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Named;
import dev.l3g7.griefer_utils.api.util.Util;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.settings.types.DropDownSetting;
import dev.l3g7.griefer_utils.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.WindowClickEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.MysteryModPayloadEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.PacketEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.Laby4Module;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;

import java.util.concurrent.TimeUnit;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;
import static net.minecraft.network.play.client.C07PacketPlayerDigging.Action.DROP_ALL_ITEMS;
import static net.minecraft.network.play.client.C07PacketPlayerDigging.Action.DROP_ITEM;

@Singleton
public class ClearLag extends Laby4Module {

	private final DropDownSetting<TimeFormat> timeFormat = DropDownSetting.create(TimeFormat.class)
		.name("Zeitformat")
		.description("In welchem Format die verbleibende Zeit angezeigt werden soll.")
		.icon("hourglass")
		.defaultValue(TimeFormat.LONG);

	private final NumberSetting warnTime = NumberSetting.create()
		.name("Warn-Zeit (s)")
		.description("Wie viele Sekunden vor dem nächsten Clearlag eine Warnung angezeigt werden soll.")
		.icon("labymod_3/exclamation_mark");

	private final SwitchSetting preventDrop = SwitchSetting.create()
		.name("Droppen verhindern")
		.description("Verhindert das Droppen von Items, wenn die Warnung angezeigt wird.")
		.icon(Blocks.dropper);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Clearlag")
		.description("Zeigt dir die Zeit bis zum nächsten Clearlag an.")
		.icon("gold_ingot_crossed_out")
		.subSettings(timeFormat, warnTime, preventDrop);

	private long clearLagEnd = -1;

	@Override
	public String getValue() {
		if (clearLagEnd == -1)
			return "Unbekannt";

		long diff = clearLagEnd - System.currentTimeMillis();
		if (diff < 0)
			return "Unbekannt";

		// Warn if clearlag is less than the set amount of seconds away
		if (diff < warnTime.get() * 1000) {
			String s = Util.formatTime(clearLagEnd, true);
			if (!s.equals("0s")) {
				mc().ingameGUI.displayTitle("§cClearlag!", null, -1, -1, -1);
				mc().ingameGUI.displayTitle(null, "§c§l" + s, -1, -1, -1);
				mc().ingameGUI.displayTitle(null, null, 0, 2, 3);
			}
		}

		return Util.formatTime(clearLagEnd, timeFormat.get() == TimeFormat.SHORT);
	}

	@EventListener
	private void onServerSwitch(ServerSwitchEvent event) {
		clearLagEnd = -1;
	}

	@EventListener
	private void onMMCustomPayload(MysteryModPayloadEvent event) {
		if (!event.channel.equals("countdown_create"))
			return;

		JsonObject countdown = event.payload.getAsJsonObject();
		if (countdown.get("name").getAsString().equals("ClearLag"))
			clearLagEnd = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(countdown.get("until").getAsInt(), TimeUnit.valueOf(countdown.get("unit").getAsString()));
	}

	@EventListener
	private void onWindowClick(WindowClickEvent event) {
		if (event.mode != 4 || !preventDrop.get())
			return;

		long diff = clearLagEnd - System.currentTimeMillis();
		if (diff > 0 && diff < warnTime.get() * 1000)
			event.cancel();
	}

	@EventListener
	private void onPacketDigging(PacketEvent.PacketSendEvent<C07PacketPlayerDigging> event) {
		if (!preventDrop.get() || (event.packet.getStatus() != DROP_ITEM && event.packet.getStatus() != DROP_ALL_ITEMS))
			return;

		long diff = clearLagEnd - System.currentTimeMillis();
		if (diff > 0 && diff < warnTime.get() * 1000)
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
