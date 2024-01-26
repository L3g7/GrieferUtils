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

package dev.l3g7.griefer_utils.laby4.settings;

import dev.l3g7.griefer_utils.api.event.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.api.util.Util;
import dev.l3g7.griefer_utils.features.Category;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.FeatureCategory;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.settings.Setting;
import net.labymod.api.configuration.settings.type.RootSettingRegistry;
import net.labymod.api.configuration.settings.type.SettingElement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainPage {

	private static final Config config = new Config();

	@OnEnable
	public static void registerSettings(Setting parent) {
		// Create root setting
		RootSettingRegistry registry = new RootSettingRegistry("griefer_utils", "settings") {
			public Component displayName() {
				return Component.text("GrieferUtils");
			}
		};
		Laby.labyAPI().coreSettingRegistry().addSetting(registry);

		// Collect settings
		ArrayList<BaseSetting<?>> settings = new ArrayList<>();
		collectSettings(settings);
		settings.sort(Comparator.comparingInt(e -> ((SettingElement) e).getOrderValue()));

		// Initialize settings
		settings.forEach(s -> ((BaseSettingImpl<?, ?>) s).create(config, parent));
		registry.addSettings(Reflection.<List<Setting>>c(settings));
	}

	// TODO: searchableSettings

	private static void collectSettings(List<BaseSetting<?>> settings) {
		// Load features
		List<Feature> features = new ArrayList<>();
		FileProvider.getClassesWithSuperClass(Feature.class).forEach(meta -> {
			if (!meta.isAbstract())
				features.add(FileProvider.getSingleton(meta.load()));
		});

		features.sort(Comparator.comparing(f -> f.getMainElement().name()));
		for (Feature feature : features) {
			feature.getCategory().add(feature);

			if (!feature.getClass().isAnnotationPresent(FeatureCategory.class))
				continue;

			// Enable the feature category when one of its features is enabled
			if (!(feature.getMainElement() instanceof SwitchSetting main))
				continue;

			for (BaseSetting<?> element : main.getSubSettings()) {
				if (!(element instanceof SwitchSetting s))
					continue;

				s.callback(b -> {
					if (b)
						main.set(true);
				});
			}
		}


		// Add every category to the main page
		Category.getCategories().stream()
			.filter(c -> c.getConfigKey() != null)
			.map(Category::getSetting)
			.sorted(Comparator.comparing(BaseSetting::name))
			.forEach(settings::add);

		settings.add(HeaderSetting.create());

		Category.getCategories().stream()
			.filter(c -> c.getConfigKey() == null)
			.flatMap(s -> s.getSetting().getSubSettings().stream())
			.sorted(Comparator.comparing(BaseSetting::name))
			.forEach(settings::add);

		settings.add(HeaderSetting.create());

		// Wiki link
		settings.add(ButtonSetting.create()
			.name("Wiki").icon("open_book")
			.buttonIcon("open_book_outline")
			.callback(() -> Util.openWebsite("https://grieferutils.l3g7.dev/wiki")));

		// Discord link
		settings.add(ButtonSetting.create()
			.name("Discord").icon("discord")
			.buttonIcon("discord_clyde")
			.callback(() -> Util.openWebsite("https://grieferutils.l3g7.dev/discord")));
	}

}
