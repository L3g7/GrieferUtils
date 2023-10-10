/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.features.modules.timers;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.util.Util;
import dev.l3g7.griefer_utils.event.events.network.MysteryModPayloadEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.DropDownSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;

import java.util.concurrent.TimeUnit;

@Singleton
public class ClearLag extends Module {

	private final DropDownSetting<TimeFormat> timeFormat = new DropDownSetting<>(TimeFormat.class)
		.name("Zeitformat")
		.icon("hourglass")
		.defaultValue(TimeFormat.LONG);

	private final NumberSetting warnTime = new NumberSetting()
		.name("Warn-Zeit (s)")
		.icon("labymod:buttons/exclamation_mark");

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Clearlag")
		.description("Zeigt dir die Zeit bis zum nächsten Clearlag an.")
		.icon("gold_ingot_crossed_out")
		.subSettings(timeFormat, warnTime);

	private long clearLagEnd = -1;

	@Override
	public String[] getValues() {
		if (clearLagEnd == -1)
			return getDefaultValues();

		long diff = clearLagEnd - System.currentTimeMillis();
		if (diff < 0)
			return getDefaultValues();

		// Warn if clearlag is less than the set amount of seconds away
		if (diff < warnTime.get() * 1000) {
			String s = Util.formatTime(clearLagEnd, true);
			if (!s.equals("0s"))
				title("§c§l" + s);
		}

		return new String[]{Util.formatTime(clearLagEnd, timeFormat.get() == TimeFormat.SHORT)};
	}

	@Override
	public String[] getDefaultValues() {
		return new String[]{"Unbekannt"};
	}

	@EventListener
	public void onServerSwitch(ServerSwitchEvent event) {
		clearLagEnd = -1;
	}

	@EventListener
	public void onMMCustomPayload(MysteryModPayloadEvent event) {
		if (!event.channel.equals("countdown_create"))
			return;

		JsonObject countdown = event.payload.getAsJsonObject();
		if (countdown.get("name").getAsString().equals("ClearLag"))
			clearLagEnd = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(countdown.get("until").getAsInt(), TimeUnit.valueOf(countdown.get("unit").getAsString()));
	}

	private void title(String title) {
		mc.ingameGUI.displayTitle("§cClearlag!", null, -1, -1, -1);
		mc.ingameGUI.displayTitle(null, title, -1, -1, -1);
		mc.ingameGUI.displayTitle(null, null, 0, 2, 3);
	}

	private enum TimeFormat {
		SHORT("Kurz"),
		LONG("Lang");

		private final String name;
		TimeFormat(String name) {
			this.name = name;
		}
	}
}