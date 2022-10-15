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

package dev.l3g7.griefer_utils.settings;

import dev.l3g7.griefer_utils.event.events.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.util.AddonUtil;
import dev.l3g7.griefer_utils.util.IOUtil;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.addon.online.info.AddonInfo;

import static net.labymod.utils.ModColor.YELLOW;

/**
 * Retrieves the addon description from the server and sets it.
 */
@Singleton
public class AddonDescriptor {

	@OnStartupComplete
	public void updateDescription() {
		AddonInfo addonInfo = AddonUtil.getInfo();
		if (addonInfo != null) {
			Reflection.set(addonInfo, "author", "L3g7 \u2503 v" + AddonUtil.getVersion());
			updateDescription(YELLOW + "Verbinde mit GrieferUtils-Server ...");

			// Load description from server, so it can be used as news board
			IOUtil.read("https://grieferutils.l3g7.dev/addon_description/")
				.asJsonString(this::updateDescription)
				.orElse(() -> updateDescription(YELLOW + "Der GrieferUtils-Server scheint nicht erreichbar zu sein :("));
		}
	}

	private void updateDescription(String description) {
		Reflection.set(AddonUtil.getInfo(), "description", description);
	}

}
