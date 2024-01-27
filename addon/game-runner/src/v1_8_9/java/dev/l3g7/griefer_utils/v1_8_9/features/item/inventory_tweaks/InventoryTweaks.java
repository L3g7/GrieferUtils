/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.item.inventory_tweaks;

import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.Feature.FeatureCategory;
import dev.l3g7.griefer_utils.settings.AbstractSetting;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.settings.SettingLoader;
import dev.l3g7.griefer_utils.settings.types.CategorySetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@FeatureCategory
public class InventoryTweaks extends Feature {

	private final List<InventoryTweak> tweaks = FileProvider.getClassesWithSuperClass(InventoryTweak.class).stream()
		.map(ClassMeta::load)
		.map(FileProvider::getSingleton)
		.map(InventoryTweak.class::cast)
		.collect(Collectors.toList());

	@MainElement
	private final CategorySetting enabled = CategorySetting.create()
		.name("Inventar verbessern")
		.description("Verbessert Interaktionen mit dem Inventar.")
		.icon("chest")
		.subSettings();

	@Override
	public void init() {
		super.init();
		for (InventoryTweak tweak : tweaks)
			tweak.init(enabled);

		tweaks.sort(Comparator.comparing(f -> f.mainElement.name()));
		enabled.subSettings(tweaks.stream().map(s -> s.mainElement).collect(Collectors.toList()));
	}

	public static abstract class InventoryTweak {

		protected BaseSetting<?> mainElement;

		protected BaseSetting<?> init(AbstractSetting<?, ?> parent) {
			return mainElement = SettingLoader.initMainElement(this, parent).mainElement;
		}

		public boolean isEnabled() {
			return FileProvider.getSingleton(InventoryTweaks.class).isEnabled() && ((SwitchSetting) mainElement).get();
		}

	}

}
