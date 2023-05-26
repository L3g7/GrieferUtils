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

package dev.l3g7.griefer_utils.features.uncategorized.settings;

import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.misc.VersionComparator;
import dev.l3g7.griefer_utils.core.util.IOUtil;
import dev.l3g7.griefer_utils.features.uncategorized.settings.auto_update.UpdateScreen;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.TextSetting;
import net.labymod.settings.elements.SettingsElement;

import java.util.*;

public class Changelog {

	public static final CategorySetting category = new CategorySetting()
		.name("§eChangelog")
		.description("§eVerbindet...")
		.icon("white_scroll")
		.settingsEnabled(false)
		.subSettings();

	static {
		IOUtil.read("https://grieferutils.l3g7.dev/v2/changelog").asJsonObject(releases -> {
			List<SettingsElement> entries = new ArrayList<>();
			for (Map.Entry<String, JsonElement> entry : releases.entrySet()) {

				if (!UpdateScreen.hasData()) {
					UpdateScreen.setData(
						entry.getKey(),
						entry.getValue().getAsString().substring("Changelog:".length())
					);
				}

				String title = "§l" + entry.getKey();

				entries.add(new CategorySetting()
					.name(" " + title)
					.subSettings(Arrays.asList(
						new HeaderSetting("§r"),
						new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
						new HeaderSetting("§e§lChangelog - " + title).scale(.7),
						new TextSetting(393).addText(entry.getValue().getAsString().replace("\r", ""))
					)));
			}

			entries.sort(Comparator.comparing(SettingsElement::getDisplayName, new VersionComparator()));
			category.subSettings(entries);

			category.name("§y§fChangelog")
				.description()
				.settingsEnabled(true);
		}).orElse(() ->
			category.name("§y§c§mChangelog")
				.description("§cEs gab einen Fehler.")
				.settingsEnabled(false)
		);
	}
}