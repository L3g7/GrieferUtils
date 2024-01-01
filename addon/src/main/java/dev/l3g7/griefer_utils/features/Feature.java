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

package dev.l3g7.griefer_utils.features;

import dev.l3g7.griefer_utils.core.event_bus.Disableable;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import net.labymod.settings.elements.SettingsElement;
import org.apache.commons.lang3.tuple.Pair;

/**
 * The base class for features.
 */
public abstract class Feature implements Disableable {

	private Category category;
	private SettingsElement mainElement;
	private String configKey;

	/**
	 * Initialises the feature.
	 */
	public void init() {

		// Find package holding category meta
		Package pkg = getClass().getPackage();
		do {
			if (pkg.isAnnotationPresent(Category.Meta.class) || pkg.isAnnotationPresent(Category.Uncategorized.class))
				break;
		} while ((pkg = Reflection.getParentPackage(pkg)) != null);

		if (pkg == null)
			throw new IllegalStateException("Could not find category of " + getClass().getPackage().getName());

		category = Category.getCategory(pkg);

		Pair<SettingsElement, String> data = ElementBuilder.initMainElement(this, category.getConfigKey());
		mainElement = data.getLeft();
		configKey = data.getRight();
	}

	public SettingsElement getMainElement() {
		return mainElement;
	}

	public Category getCategory() {
		return category;
	}

	/**
	 * Checks if the parent category and the feature itself is enabled.
	 */
	public boolean isEnabled() {
		if (!category.isEnabled())
			return false;

		if (mainElement instanceof BooleanSetting)
			return ((BooleanSetting) mainElement).get();
		if (mainElement instanceof NumberSetting)
			return ((NumberSetting) mainElement).get() != 0;
		return true;
	}

	public String getConfigKey() {
		return configKey;
	}

}
