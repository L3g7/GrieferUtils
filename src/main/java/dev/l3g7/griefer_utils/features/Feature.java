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

package dev.l3g7.griefer_utils.features;

import dev.l3g7.griefer_utils.event.EventHandler;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent.ServerJoinEvent;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent.ServerSwitchEvent;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.settings.elements.SettingsElement;
import org.apache.commons.lang3.tuple.Pair;

/**
 * The base class for features.
 */
public abstract class Feature {

	private static boolean onCityBuild;

	private Category category;
	private SettingsElement mainElement;
	private String configKey;

	/**
	 * <p>
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

		// Register events
		EventHandler.register(this);
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

	public boolean isOnCityBuild() {
		return onCityBuild;
	}

	@EventListener
	private static void _onServerJoin(ServerJoinEvent event) {
		onCityBuild = true;
	}

	@EventListener
	private static void _onServerSwitch(ServerSwitchEvent event) {
		onCityBuild = false;
	}

}
