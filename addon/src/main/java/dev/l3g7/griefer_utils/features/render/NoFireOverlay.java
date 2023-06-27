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

package dev.l3g7.griefer_utils.features.render;

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.render.BurningCheckEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

/**
 * Hides the fire overlay visible when burning.
 */
@Singleton
public class NoFireOverlay extends Feature {

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Feuer-Overlay entfernen")
		.description("§r§fDeaktiviert den Feuer-Effekt im First-Person-Modus.", "§l§nNur benutzen, wenn man Feuerresistenz besitzt!")
		.icon("fire");

	@EventListener
	public void onDisplayNameRender(BurningCheckEvent event) {
		// Only hide if in first person
		event.setCanceled(mc().gameSettings.thirdPersonView == 0);
	}

}
