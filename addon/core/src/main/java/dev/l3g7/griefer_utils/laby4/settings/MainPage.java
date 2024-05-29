/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4.settings;

import dev.l3g7.griefer_utils.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.api.event.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.Feature.FeatureCategory;
import dev.l3g7.griefer_utils.laby4.settings.types.SwitchSettingImpl;
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
import net.labymod.api.configuration.settings.type.SettingElement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_4;
import static dev.l3g7.griefer_utils.api.bridges.LabyBridge.labyBridge;

@ExclusiveTo(LABY_4)
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

	private static void collectSettings(List<BaseSetting<?>> settings) {
		// Enable the feature category if one of its features gets enabled
		Feature.getFeatures()
			.sorted(Comparator.comparing(f -> f.getMainElement().name()))
			.forEach(feature -> {
				if (!feature.getClass().isAnnotationPresent(FeatureCategory.class)
					|| !(feature.getMainElement() instanceof SwitchSettingImpl main))
					return;

				for (BaseSetting<?> element : main.getChildSettings()) {
					if (!(element instanceof SwitchSetting sub))
						continue;

					sub.callback(b -> {
						if (b)
							main.set(true);
					});
				}

				main.setSearchTags(new String[]{main.name()});
			});

		// Add features to categories
		Feature.getFeatures()
			.sorted(Comparator.comparing(f -> f.getMainElement().name()))
			.forEach(f -> {
				f.addToCategory();
				((SettingElement) f.getMainElement()).setSearchTags(new String[]{f.getMainElement().name()});
			});

		// Add categories
		Feature.getCategories().stream()
			.sorted(Comparator.comparing(BaseSetting::name))
			.forEach(settings::add);

		for (SwitchSetting v : Feature.getCategories())
			((SwitchSettingImpl) v).setSearchTags(new String[]{v.name()});

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

		// Ko-fi link
		settings.add(ButtonSetting.create()
			.name("Entwickler unterstützen").icon("ko_fi")
			.description("Wenn dir das Addon gefällt kannst du hier das Entwickler-Team dahinter unterstützen §c❤")
			.buttonIcon("ko_fi_outline")
			.callback(() -> labyBridge.openWebsite("https://ko-fi.com/l3g7_3")));

		// Discord link
		settings.add(ButtonSetting.create()
			.name("Discord").icon("discord")
			.buttonIcon("discord_clyde")
			.callback(() -> labyBridge.openWebsite("https://grieferutils.l3g7.dev/discord")));
	}

	private static class RootSetting extends RootSettingRegistry {

		private RootSetting() {
			super(Laby4Util.getNamespace(), Laby4Util.getNamespace());
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
