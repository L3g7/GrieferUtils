/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.features.uncategorized.settings;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.event.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.settings.ValueHolder;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.IOUtil;

import java.util.Collections;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.uuid;

public class Telemetry {

	private static final BooleanSetting uuid = new BooleanSetting()
		.name("UUID")
		.description("Ob deine Minecraft-UUID mitgesendet werden soll.")
		.config("settings.telemetry.uuid")
		.icon("steve")
		.defaultValue(true)
		.callback(v -> updateTelemetrySettings());

	private static final BooleanSetting ping = new BooleanSetting()
		.name("Ping")
		.description("Ob der Telemetrie-Server angepingt werden soll, um zu signalisieren, dass GrieferUtils installiert ist.")
		.config("settings.telemetry.ping")
		.icon("one_player_plaque")
		.defaultValue(true)
		.callback(v -> { if (!v) updateTelemetrySettings(); });

	private static final List<BooleanSetting> telemetrySettings = Collections.singletonList(uuid);

	public static final CategorySetting category = new CategorySetting()
		.name("Telemetrie")
		.description("GrieferUtils sammelt Nutzungsdaten, um die Benutzerfreundlichkeit zu verbessern. Da die Daten uns helfen, würde es uns freuen, wenn sie gesendet werden ^.^", "", "§7§oUm Spam vorzubeugen, wird zusätzlich zu den einstellbaren Daten ein Hash deiner IP gespeichert. Alle erhobenen Daten werden bis zu 365 Tage lang gespeichert. Falls du die erhobenen Daten erhalten oder löschen willst, melde dich bei einem Entwickler über Discord oder schreibe eine Email an grieferutils@l3g7.dev")
		.icon("magnifying_glass")
		.subSettings(telemetrySettings.toArray(new BooleanSetting[0]))
		.subSettings(Collections.singletonList(ping));

	@OnEnable
	private static void updateTelemetrySettings() {
		boolean telemetryActive = telemetrySettings.stream().anyMatch(ValueHolder::get);
		if (telemetryActive) {
			ping.name("§7Ping")
				.description("Ob der Telemetrie-Server angepingt werden soll, um zu signalisieren, dass GrieferUtils installiert ist.", "", "§lUm Pings zu deaktivieren, muss jede Telemetrieoption deaktiviert sein.")
				.set(true);
		} else {
			ping.name("Ping")
				.description("Ob der Telemetrie-Server angepingt werden soll, um zu signalisieren, dass GrieferUtils installiert ist.");
		}
	}

	@OnEnable
	private static void sendTelemetryData() {
		if (!ping.get())
			return;

		JsonObject data = new JsonObject();

		if (uuid.get())
			data.addProperty("uuid", uuid().toString());

		IOUtil.writeJson("https://grieferutils.l3g7.dev/v2/telemetry", data);
	}

}