/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.settings;

import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.api.util.StringUtil;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.features.Feature.MainElement;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import static dev.l3g7.griefer_utils.core.api.util.Util.elevate;

public class SettingLoader { // NOTE: cleanup

	public static MainElementData initMainElement(Object owner, String parentKey) {
		Class<?> ownerClass = owner.getClass();

		// Load main element
		Field[] mainElementFields = Reflection.getAnnotatedFields(ownerClass, MainElement.class, false);
		if (mainElementFields.length != 1)
			throw new IllegalStateException("Found an invalid amount of main elements for " + ownerClass.getSimpleName());

		Field mainElementField = mainElementFields[0];
		BaseSetting<?> mainElement = Reflection.get(owner, mainElementField);

		// Load config key
		String configKey = StringUtil.convertCasing(ownerClass.getSimpleName());
		if (parentKey != null)
			configKey = parentKey + "." + configKey;

		// Load settings
		if (mainElement instanceof AbstractSetting<?, ?>)
			((AbstractSetting<?, ?>) mainElement).config(configKey + "." + mainElementField.getName());

		if (mainElementField.getAnnotation(MainElement.class).configureSubSettings())
			loadSubSettings(owner, mainElement, configKey);

		return new MainElementData(mainElement, configKey);
	}

	/**
	 * Loads the config values for all subSettings.
	 */
	private static void loadSubSettings(Object owner, BaseSetting<?> parent, String parentKey) {
		for (BaseSetting<?> element : new ArrayList<>(parent.getChildSettings())) {
			boolean hasSubSettings = !element.getChildSettings().isEmpty();
			if (!hasSubSettings && !(element instanceof AbstractSetting<?, ?>))
				continue;

			Field field = Arrays.stream(Reflection.getAllFields(owner.getClass()))
				.filter(f -> Reflection.get(owner, f) == element)
				.findFirst()
				.orElse(null);

			if (field == null) {
				// Skip dynamic settings with inferred keys
				if (element instanceof AbstractSetting<?, ?> abs && abs.getStorage().configKey != null)
					continue;

				// Allow dynamic settings if they don't hold values and have no settings
				if (((AbstractSetting<?, ?>) element).get() == BaseSetting.NULL && element.getChildSettings().isEmpty())
					continue;

				throw elevate(new NoSuchFieldException(), "Could not find declaration field for " + element.name() + " in " + owner);
			}

			if (field.getName().equals("value"))
				throw elevate(new IllegalStateException(), field + " has an illegal name!");

			String key = parentKey + "." + StringUtil.convertCasing(field.getName());
			load(owner, element, key, field.getDeclaringClass().getSimpleName() + "." + field.getName());
		}
	}

	private static void load(Object owner, BaseSetting<?> element, String key, String identifier) {
		if (element instanceof AbstractSetting<?, ?> abs) {
			try {
				if (!element.getChildSettings().isEmpty() && abs.getStorage().subsettingConfig)
					abs.config(key + ".value");
				else
					abs.config(key);
			} catch (Throwable t) {
				throw Util.elevate(t, "loading config for %s failed!", identifier);
			}

			if (abs.getStorage().subsettingConfig)
				loadSubSettings(owner, element, key);
		} else
			loadSubSettings(owner, element, key);
	}

	public static class MainElementData {

		public final BaseSetting<?> mainElement;
		public final String configKey;

		private MainElementData(BaseSetting<?> mainElement, String configKey) {
			this.mainElement = mainElement;
			this.configKey = configKey;
		}

	}
}
