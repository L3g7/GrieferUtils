/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4.settings;

import dev.l3g7.griefer_utils.api.event.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.Feature.FeatureCategory;
import dev.l3g7.griefer_utils.laby4.util.Laby4Util;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.type.RootSettingRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.labyBridge;

public class MainPage {

	@OnEnable
	public static void registerSettings() {
		// Create root setting
		RootSetting registry = new RootSetting();
		Laby.labyAPI().coreSettingRegistry().addSetting(registry);

		// Collect settings
		ArrayList<BaseSetting<?>> settings = new ArrayList<>();
		collectSettings(settings);

		// Initialize settings
		settings.forEach(s -> {
			if (s instanceof BaseSettingImpl<?, ?> b)
				b.create(registry);
		});
		registry.addSettings(Reflection.<List<Setting>>c(settings));
	}

	// TODO: searchableSettings

	private static void collectSettings(List<BaseSetting<?>> settings) {
		// Enable the feature category if one of its features gets enabled
		Feature.getFeatures()
			.sorted(Comparator.comparing(f -> f.getMainElement().name()))
			.forEach(feature -> {
				if (!feature.getClass().isAnnotationPresent(FeatureCategory.class)
					|| !(feature.getMainElement() instanceof SwitchSetting main))
					return;

				for (BaseSetting<?> element : main.getSubSettings()) {
					if (!(element instanceof SwitchSetting sub))
						continue;

					sub.callback(b -> {
						if (b)
							main.set(true);
					});
				}
			});

		// Add features to categories
		Feature.getFeatures()
			.sorted(Comparator.comparing(f -> f.getMainElement().name()))
			.forEach(Feature::addToCategory);

		// Add categories
		Feature.getCategories().stream()
			.sorted(Comparator.comparing(BaseSetting::name))
			.forEach(settings::add);

		settings.add(HeaderSetting.create());

		// Add uncategorized features
		Feature.getUncategorized().stream()
			.sorted(Comparator.comparing(BaseSetting::name))
			.forEach(settings::add);

		settings.add(HeaderSetting.create());

		// Wiki link
		settings.add(ButtonSetting.create()
			.name("Wiki").icon("open_book")
			.buttonIcon("open_book_outline")
			.callback(() -> labyBridge.openWebsite("https://grieferutils.l3g7.dev/wiki")));

		// Discord link
		settings.add(ButtonSetting.create()
			.name("Discord").icon("discord")
			.buttonIcon("discord_clyde")
			.callback(() -> labyBridge.openWebsite("https://grieferutils.l3g7.dev/discord")));
	}

	private static class RootSetting extends RootSettingRegistry {

		private RootSetting() {
			super(Laby4Util.getNamespace(), "settings");
		}

		@Override
		public Component displayName() {
			return Component.text("GrieferUtils");
		}

		@Override
		public Icon getIcon() {
			return SettingsImpl.buildIcon("icon");
		}

	}

}
