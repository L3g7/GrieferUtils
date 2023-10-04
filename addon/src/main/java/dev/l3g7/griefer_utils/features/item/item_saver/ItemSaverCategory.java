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

package dev.l3g7.griefer_utils.features.item.item_saver;

import dev.l3g7.griefer_utils.core.event_bus.Disableable;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.FeatureCategory;
import dev.l3g7.griefer_utils.features.item.item_saver.tool_saver.ToolSaver;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.CategorySetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import net.labymod.settings.elements.SettingsElement;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@FeatureCategory
public class ItemSaverCategory extends Feature {

	private final List<Class<?>> savers = Arrays.asList(
		dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver.ItemSaver.class,
		BorderSaver.class,
		ParticleSaver.class,
		PrefixSaver.class,
		ToolSaver.class,
		HeadSaver.class,
		ArmorBreakWarning.class,
		OrbSaver.class
	);

	@MainElement
	private final CategorySetting category = new CategorySetting()
		.name("Item-Schutz")
		.description("Schützt Items vor unabsichtlicher Zerstörung.")
		.icon("shield_with_sword")
		.subSettings();

	@Override
	public void init() {
		super.init();

		// Get savers
		List<ItemSaver> savers = this.savers.stream()
				.map(FileProvider::getSingleton)
				.map(ItemSaver.class::cast)
				.collect(Collectors.toList());

		// Add savers to category
		category.subSettings(savers.stream()
			.map(saver -> saver.init(getCategory().getConfigKey()))
			.sorted(Comparator.comparing(SettingsElement::getDisplayName))
			.collect(Collectors.toList()));
	}

	public static abstract class ItemSaver implements Disableable {

		protected SettingsElement mainElement;
		protected String configKey;

		protected void init() {}

		protected String getConfigKey() {
			return configKey;
		}

		protected SettingsElement init(String parentConfigKey) {
			Pair<SettingsElement, String> data = ElementBuilder.initMainElement(this, parentConfigKey);
			mainElement = data.getLeft();
			configKey = data.getRight();

			init();
			return mainElement;
		}

		public boolean isEnabled() {
			if (!FileProvider.getSingleton(ItemSaverCategory.class).isEnabled())
				return false;

			if (mainElement instanceof BooleanSetting)
				return ((BooleanSetting) mainElement).get();
			if (mainElement instanceof NumberSetting)
				return ((NumberSetting) mainElement).get() != 0;
			return true;
		}

	}

}
