/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.settings;

import dev.l3g7.griefer_utils.features.Category;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.file_provider.meta.ClassMeta;
import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import net.labymod.settings.elements.SettingsElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * The main page of the addon's settings.
 */
public class MainPage {

	public static final List<SettingsElement> settings = new ArrayList<>(Arrays.asList(
		new HeaderSetting("§r"),
		new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
		new HeaderSetting("§e§lStartseite").scale(.7),
		new HeaderSetting("§r").scale(.4).entryHeight(10)));

	static {
		// Load features
		FileProvider.getClassesWithSuperClass(Feature.class).stream().sorted(Comparator.comparing(o -> o.name)).forEach(meta -> {
			if (meta.isAbstract())
				return;

			Feature instance = FileProvider.getSingleton(meta.load());
			instance.init();
		});

		// Add every category to the main page
		for (Category category : Category.getCategories())
			settings.add(category.getSetting());
	}

}
