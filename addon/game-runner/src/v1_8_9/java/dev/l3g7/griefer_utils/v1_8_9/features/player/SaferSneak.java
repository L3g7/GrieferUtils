/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.v1_8_9.features.player;

import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.types.SliderSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.ShiftAirCheckEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.Feature;

@Singleton
public class SaferSneak extends Feature {

	private final SliderSetting minFallDistance = SliderSetting.create()
		.name("Minimale Falldistanz")
		.description("Die minimale Differenz zwischen dir und dem Block unter dir, in Prozent einer Block-Höhe."
			+ "\n\n§7Referenz-Höhen:"
			+ "\n§7Kiste: 87%"
			+ "\n§7Stufen: 50%")
		.icon("ruler")
		.defaultValue(87)
		.max(100)
		.min(1);

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Sichereres Sneaken")
		.description("Verringert die minimale Falldistanz, bei der Sneaken das Fallen verhindert.")
		.icon("sneaking")
		.subSettings(minFallDistance);

	@EventListener
	private void onShiftAirCheck(ShiftAirCheckEvent event) {
		event.boundingBoxOffset = minFallDistance.get() / -100d;
	}

}
