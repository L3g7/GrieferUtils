/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.item_saver;

import dev.l3g7.griefer_utils.core.api.event_bus.Disableable;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.Feature.FeatureCategory;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.SettingLoader;
import dev.l3g7.griefer_utils.core.settings.SettingLoader.MainElementData;
import dev.l3g7.griefer_utils.core.settings.types.CategorySetting;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver.TempItemSaverBridge;
import dev.l3g7.griefer_utils.features.item.item_saver.tool_saver.TempToolSaverBridge;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@FeatureCategory
public class ItemSaverCategory extends Feature {

	private final List<Class<?>> savers = Arrays.asList(
		// dev.l3g7.griefer_utils.features.item.item_saver.specific_item_saver.laby4.ItemSaver.class,
		BorderSaver.class,
		ParticleSaver.class,
		PrefixSaver.class,
		// ToolSaver.class,
		HeadSaver.class,
		ArmorBreakWarning.class,
		OrbSaver.class
	);

	@MainElement
	private final CategorySetting category = CategorySetting.create()
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

		savers.add((ItemSaver) FileProvider.getBridge(TempItemSaverBridge.class)); // TODO: temp
		savers.add((ItemSaver) FileProvider.getBridge(TempToolSaverBridge.class));

		// Add savers to category
		category.subSettings(savers.stream()
			.map(saver -> saver.init(getCategory().configKey()))
			.sorted(Comparator.comparing(BaseSetting::name))
			.collect(Collectors.toList()));
	}

	public static abstract class ItemSaver implements Disableable {

		protected BaseSetting<?> mainElement;
		protected String configKey;

		protected void init() {}

		protected String getConfigKey() {
			return configKey;
		}

		protected BaseSetting<?> init(String parentKey) {
			MainElementData data = SettingLoader.initMainElement(this, parentKey);
			mainElement = data.mainElement;
			configKey = data.configKey;

			init();
			return mainElement;
		}

		public boolean isEnabled() {
			if (!FileProvider.getSingleton(ItemSaverCategory.class).isEnabled())
				return false;

			if (mainElement instanceof SwitchSetting)
				return ((SwitchSetting) mainElement).get();
			if (mainElement instanceof NumberSetting)
				return ((NumberSetting) mainElement).get() != 0;
			return true;
		}

	}

}
