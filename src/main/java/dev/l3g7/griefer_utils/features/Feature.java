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
import dev.l3g7.griefer_utils.settings.ValueHolder;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.settings.elements.SettingsElement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.util.Util.elevate;

/**
 * The base class for features.
 */
public abstract class Feature implements MinecraftUtil {

	private Category category;
	private SettingsElement mainElement;

	/**
	 * <p>
	 * Initialises the feature.
	 * </p><p>
	 * This can sadly not be done in the constructor as non-static fields are accessed.
	 * If anyone knows a better way of doing this, let me know ;D
	 * </p>
	 */
	public void init() {
		category = Category.getCategory(getClass().getPackage());

		// Load main element
		Field[] mainElementFields = Reflection.getAnnotatedFields(getClass(), MainElement.class);
		if (mainElementFields.length != 1)
			throw new IllegalStateException("Found an invalid amount of main elements");

		mainElement = Reflection.get(this, mainElementFields[0]);

		// Load config key
		String configKey = getClass().getSimpleName();
		configKey = category.getConfigKey() + "." + convertCamelCaseToSnakeCase(configKey);

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
		for (SettingsElement element : parent.getSubSettings().getElements()) {
			String key = parentKey + "." + getFieldName(element);
			loadSubSettings(element, key);
			if (element instanceof ValueHolder<?, ?>)
				((ValueHolder<?, ?>) element).config(key);
		}
	}

	/**
	 * Gets the name of a field based on it's value.
	 */
	private String getFieldName(SettingsElement element) {
		for (Field field : getClass().getDeclaredFields())
			if (Reflection.get(this, field) == element)
				return field.getName();

		throw elevate(new NoSuchFieldException(), "Could not find declaration field for " + element.getDisplayName());
	}

	public SettingsElement getMainElement() {
		return mainElement;
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

	private static final Pattern CAPS_PATTERN = Pattern.compile("([A-Z])");

	/**
	 * Converts a CamelCase string to snake_case
	 */
	private static String convertCamelCaseToSnakeCase(String str) {
		Matcher matcher = CAPS_PATTERN.matcher(str);
		while (matcher.find())
			str = str.replaceFirst(matcher.group(1),  (matcher.start() == 0 ? "" : "_") + matcher.group(1).toLowerCase());
		return str;
	}

	/**
	 * An annotation for marking the main element in a feature.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface MainElement { }

}
