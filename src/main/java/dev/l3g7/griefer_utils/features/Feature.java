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

import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.settings.ValueHolder;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.NumberSetting;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.settings.elements.SettingsElement;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.util.Util.elevate;

/**
 * The base class for features.
 */
public abstract class Feature implements MinecraftUtil {

	private static final Pattern CAPS_PATTERN = Pattern.compile("([A-Z])");
	private Category category;
	private SettingsElement mainElement;

	/**
	 * IDk what is happening here, I'm just the guy writing the docs
	 */
	public void init() {
		category = Category.getCategory(getClass().getPackage());

		// Load main element
		Field[] mainElementFields = Reflection.getAnnotatedFields(getClass(), MainElement.class);
		if (mainElementFields.length == 0)
			throw new IllegalStateException("Could not find a main element");

		if (mainElementFields.length > 1)
			throw new IllegalStateException("Found multiple main elements");

		mainElement = Reflection.get(this, mainElementFields[0]);

		// Load config key
		String configKey = getClass().getSimpleName();
		Matcher matcher = CAPS_PATTERN.matcher(configKey);
		while (matcher.find())
			configKey = configKey.replaceFirst(matcher.group(1),  (matcher.start() == 0 ? "" : "_") + matcher.group(1).toLowerCase());
		configKey = category.getConfigKey() + "." + configKey;

		// Load settings
		if (mainElement instanceof ValueHolder<?, ?>)
			((ValueHolder<?, ?>) mainElement).config(configKey + "." + mainElementFields[0].getName());
		loadSubSettings(mainElement, configKey);

		// Register events
		for (Method method : Reflection.getAnnotatedMethods(getClass(), EventListener.class)) {
			EventListener meta = method.getDeclaredAnnotation(EventListener.class);
			Class<?>[] parameters = method.getParameterTypes();
			if (parameters.length != 1)
				throw new IllegalArgumentException("Method " + method + " has @EventListener annotation, but requires " + parameters.length + " arguments");

			Class<?> eventClass = parameters[0];

			if (!Event.class.isAssignableFrom(eventClass))
				throw new IllegalArgumentException("Method " + method + " has @EventListener annotation, but takes " + eventClass);

			Event event = (Event) Reflection.construct(eventClass);
			event.getListenerList().register(0, meta.priority(), e -> {
				if ((!event.isCancelable() || !event.isCanceled() || meta.receiveCanceled())
				&& (meta.triggerWhenDisabled() || isEnabled()))
					Reflection.invoke(this, method, e);
			});
		}
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void loadSubSettings(SettingsElement parent, String parentKey) {
		for (SettingsElement element : parent.getSubSettings().getElements()) {
			String key = parentKey + "." + getFieldName(element);
			loadSubSettings(element, key);
			if (element instanceof ValueHolder<?, ?>)
				((ValueHolder<?, ?>) element).config(key);
		}
	}

	private String getFieldName(SettingsElement element) {
		for (Field field : getClass().getDeclaredFields())
			if (Reflection.get(this, field) == element)
				return field.getName();

		throw elevate(new NoSuchFieldException(), "Could not find declaration field for " + element.getDisplayName());
	}

	public SettingsElement getMainElement() {
		return mainElement;
	}

	public boolean isEnabled() {
		if (!category.isEnabled())
			return false;

		if (mainElement instanceof BooleanSetting)
			return ((BooleanSetting) mainElement).get();
		if (mainElement instanceof NumberSetting)
			return ((NumberSetting) mainElement).get() != 0;
		return true;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface MainElement { }

}
