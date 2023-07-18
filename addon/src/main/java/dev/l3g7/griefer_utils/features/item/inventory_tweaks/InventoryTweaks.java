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

package dev.l3g7.griefer_utils.features.item.inventory_tweaks;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import net.labymod.settings.elements.SettingsElement;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class InventoryTweaks extends Feature {

	private final List<InventoryTweak> tweaks = FileProvider.getClassesWithSuperClass(InventoryTweak.class).stream()
		.map(ClassMeta::load)
		.map(FileProvider::getSingleton)
		.map(InventoryTweak.class::cast)
		.collect(Collectors.toList());

	@MainElement
	private final CategorySetting enabled = new CategorySetting()
		.name("Inventar verbessern")
		.description("Verbessert Interaktionen mit dem Inventar.")
		.icon("chest")
		.subSettings();

	@Override
	public void init() {
		super.init();
		for (InventoryTweak tweak : tweaks)
			tweak.init(getConfigKey());

		tweaks.sort(Comparator.comparing(f -> f.mainElement.getDisplayName()));
		enabled.subSettings(tweaks.stream().map(s -> s.mainElement).collect(Collectors.toList()));
	}

	public static abstract class InventoryTweak {

		protected SettingsElement mainElement;

		protected SettingsElement init(String parentConfigKey) {
			return mainElement = ElementBuilder.initMainElement(this, parentConfigKey).getLeft();
		}

		public boolean isEnabled() {
			return FileProvider.getSingleton(InventoryTweaks.class).isEnabled() && ((BooleanSetting) mainElement).get();
		}

	}

}
