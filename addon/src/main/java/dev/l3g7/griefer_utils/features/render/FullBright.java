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

package dev.l3g7.griefer_utils.features.render;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.config.Config;
import dev.l3g7.griefer_utils.event.events.TickEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.KeySetting;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

/**
 * Turns the gamma to 10.
 */
@Singleton
public class FullBright extends Feature {

	private static final String GAMMA_PATH = "render.full_bright.old_gamma_value";

	private final KeySetting key = new KeySetting()
		.name("Taste")
		.icon("key")
		.description("Die Taste, mit der FullBright an-/ausgeschalten wird.")
		.pressCallback(pressed -> {
			if (pressed) {
				BooleanSetting enabled = ((BooleanSetting) getMainElement());
				enabled.set(!enabled.get());
			}
		});

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("FullBright")
		.description("Stellt den Gammawert auf 10.")
		.icon("light_bulb")
		.subSettings(key)
		.callback(active -> {
			if (active) {
				float gamma = mc().gameSettings.gammaSetting;
				if (gamma <= 1)
					Config.set(GAMMA_PATH, new JsonPrimitive(gamma));

				return;
			}

			if (Config.has(GAMMA_PATH))
				mc().gameSettings.gammaSetting = Config.get(GAMMA_PATH).getAsFloat();
		});

	@EventListener
	public void onPlayerTick(TickEvent.ClientTickEvent event) {
		mc().gameSettings.gammaSetting = 10;
	}

}
