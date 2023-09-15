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

package dev.l3g7.griefer_utils.settings;

import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.event.events.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.event.events.network.WebDataReceiveEvent;
import dev.l3g7.griefer_utils.util.AddonUtil;
import net.labymod.addon.online.info.AddonInfo;

import static net.labymod.utils.ModColor.YELLOW;

/**
 * Retrieves the addon description from the server and sets it.
 */
@Singleton
public class AddonDescriptor {

	private String description = YELLOW + "Der GrieferUtils-Server scheint nicht erreichbar zu sein :(";
	private boolean completedStep = false;

	@EventListener
	private void onWebData(WebDataReceiveEvent event) {
		// Load description from server, so it can be used as news board
		description = event.data.addonDescription;

		updateDescription();
	}

	@OnStartupComplete
	public void updateDescription() {
		if (!completedStep) {
			completedStep = true;
			return;
		}

		AddonInfo addonInfo = AddonUtil.getInfo();
		if (addonInfo == null)
			return;

		Reflection.set(addonInfo, "L3g7, L3g73 \u2503 v" + AddonUtil.getVersion(), "author");
		Reflection.set(AddonUtil.getInfo(), description, "description");
	}

}