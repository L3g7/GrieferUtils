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
import dev.l3g7.griefer_utils.settings.ValueHolder;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import dev.l3g7.griefer_utils.util.ArrayUtil;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.settings.elements.SettingsElement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static dev.l3g7.griefer_utils.util.Util.elevate;

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
			if (pkg.isAnnotationPresent(Category.Meta.class))
				break;
		} while ((pkg = Reflection.getParentPackage(pkg)) != null);

		if (pkg == null)
			throw new IllegalStateException("Could not find category of " + getClass().getPackage().getName());

		category = Category.getCategory(pkg);

		// Load main element
		Field[] mainElementFields = Reflection.getAnnotatedFields(getClass(), MainElement.class, false);
		if (mainElementFields.length != 1)
			throw new IllegalStateException("Found an invalid amount of main elements for " + getClass().getSimpleName());

		mainElement = Reflection.get(this, mainElementFields[0]);

		// Load config key
		configKey = getClass().getSimpleName();
		configKey = category.getConfigKey() + "." + UPPER_CAMEL.to(LOWER_UNDERSCORE, configKey);

		// Load settings
		if (mainElement instanceof ValueHolder<?, ?>)
			((ValueHolder<?, ?>) mainElement).config(configKey + "." + mainElementFields[0].getName());
		loadSubSettings(mainElement, configKey);

		// Register events
		EventHandler.register(this);
	}

	/**
	 * Loads the config values for all subSettings.
	 */
	private void loadSubSettings(SettingsElement parent, String parentKey) {
		for (SettingsElement element : new ArrayList<>(parent.getSubSettings().getElements())) {
			if (element instanceof HeaderSetting)
				continue;

			boolean hasSubSettings = !element.getSubSettings().getElements().isEmpty();
			if (!hasSubSettings && !(element instanceof ValueHolder<?, ?>))
				continue;

			String key = parentKey + "." + UPPER_CAMEL.to(LOWER_UNDERSCORE, getFieldName(element));
			loadSubSettings(element, key);
			if (element instanceof ValueHolder<?, ?>) {
				if (hasSubSettings)
					((ValueHolder<?, ?>) element).config(key + ".value");
				else
					((ValueHolder<?, ?>) element).config(key);
			}
		}
	}

	/**
	 * Gets the name of a field based on its value.
	 */
	private String getFieldName(SettingsElement element) {
		for (Field field : ArrayUtil.flatmap(Field.class, getClass().getDeclaredFields(), getClass().getFields()))
			if (Reflection.get(this, field) == element)
				return field.getName();

		throw elevate(new NoSuchFieldException(), "Could not find declaration field for " + element.getDisplayName() + " in " + this);
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

	/**
	 * An annotation for marking the main element in a feature.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface MainElement { }

}
