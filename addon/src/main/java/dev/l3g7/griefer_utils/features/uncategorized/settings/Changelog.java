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
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.TextSetting;
import dev.l3g7.griefer_utils.util.IOUtil;
import dev.l3g7.griefer_utils.util.misc.Constants;
import dev.l3g7.griefer_utils.util.misc.VersionComparator;
import net.labymod.settings.elements.SettingsElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Changelog {

	public static final CategorySetting category = new CategorySetting()
		.name("§eChangelog")
		.description("§eVerbindet...")
		.icon("white_scroll")
		.settingsEnabled(false)
		.subSettings(
			new CategorySetting()
				.name(" §lv2.0-BETA")
				.description("Changelog-Informationen sind für die Beta-Version nicht verfügbar.", "Der v2-Changelog kann auf Discord gefunden werden."),
			new HeaderSetting());

	static {
		IOUtil.read("https://api.github.com/repos/L3g7/GrieferUtils/releases").asJsonArray(releases -> {
			List<SettingsElement> entries = new ArrayList<>();
			for (JsonElement releaseElement : releases) {
				JsonObject release = releaseElement.getAsJsonObject();
				String title = "§l" + release.get("tag_name").getAsString();

				entries.add(new CategorySetting()
					.name(" " + title)
					.subSettings(Arrays.asList(
						new HeaderSetting("§r"),
						new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
						new HeaderSetting("§e§lChangelog - " + title).scale(.7),
						new TextSetting(release.get("body").getAsString().replace("\r", ""))
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