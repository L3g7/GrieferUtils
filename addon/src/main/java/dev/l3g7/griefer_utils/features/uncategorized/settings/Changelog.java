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

package dev.l3g7.griefer_utils.features.uncategorized.settings;

import dev.l3g7.griefer_utils.core.auto_update.ReleaseInfo;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.VersionComparator;
import dev.l3g7.griefer_utils.core.misc.config.ConfigPatcher;
import dev.l3g7.griefer_utils.event.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.event.events.network.WebDataReceiveEvent;
import dev.l3g7.griefer_utils.misc.gui.guis.ChangelogScreen;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.settings.elements.SmallButtonSetting;
import dev.l3g7.griefer_utils.util.AddonUtil;
import net.labymod.settings.elements.SettingsElement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Singleton
public class Changelog {

	public static final CategorySetting changelog = new CategorySetting()
		.name("§eChangelog")
		.description("§eVerbindet...")
		.icon("white_scroll")
		.settingsEnabled(false)
		.subSettings();

	@EventListener
	private void onWebData(WebDataReceiveEvent event) {
		List<SettingsElement> entries = new ArrayList<>();

		if (Settings.releaseChannel.get() == ReleaseInfo.ReleaseChannel.BETA)
			ChangelogScreen.setData(AddonUtil.getVersion(), event.data.changelog.beta.substring("Changelog:".length()));

		for (Map.Entry<String, String> entry : event.data.changelog.all.entrySet()) {
			if (!ChangelogScreen.hasData()) {
				ChangelogScreen.setData(
					entry.getKey(),
					entry.getValue().substring("Changelog:".length())
				);
			}

			String title = "§l" + entry.getKey();

			entries.add(new SmallButtonSetting()
				.name(" " + title)
				.callback(() -> {
					ChangelogScreen.setData(
						entry.getKey(),
						entry.getValue().substring("Changelog:".length())
					);
					ChangelogScreen.trigger(true);
				}));
		}

		entries.sort(Comparator.comparing(SettingsElement::getDisplayName, new VersionComparator()));
		changelog.subSettings(entries);

		changelog.name("Changelog")
			.description("Was sich in den einzelnen Updates von GrieferUtils verändert hat.")
			.settingsEnabled(true);
	}

	@OnEnable
	public void onEnable() {
		if (ConfigPatcher.versionChanged)
			ChangelogScreen.trigger(false);
	}
}