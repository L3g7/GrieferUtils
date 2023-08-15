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

package dev.l3g7.griefer_utils.features.player;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.event.events.TickEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.settings;

/**
 * Automatically sprints when walking.
 */
@Singleton
public class AutoSprint extends Feature {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Automatisch sprinten")
		.description("Sprintet automatisch.")
		.icon("speed");

	@EventListener
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (player() == null || !settings().keyBindForward.isKeyDown())
			return;

		try {
			player().setSprinting(true);
		} catch (IllegalArgumentException e) {
			if (e.getMessage().equals("Modifier is already applied on this attribute!"))
				// Ignore this error, caused by asynchronous access to player's attribute modifiers
				return;

			throw e;
		}
	}

}
