/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features;

import dev.l3g7.griefer_utils.core.api.event.event_bus.Disableable;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.SettingLoader;
import dev.l3g7.griefer_utils.core.settings.SettingLoader.MainElementData;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The base class for features.
 */
public abstract class Feature implements Disableable {

	// Name to setting
	private static final Map<String, SwitchSetting> categories = new HashMap<>();

	private final SwitchSetting category = findCategory(getClass().getPackage());
	private BaseSetting<?> mainElement;
	private String configKey;

	private SwitchSetting findCategory(Package pkg) {
		if (pkg == null)
			return categories.computeIfAbsent(null, p -> SwitchSetting.create().defaultValue(true));

		if (pkg.isAnnotationPresent(Category.class)) {
			Category meta = pkg.getAnnotation(Category.class);
			return categories.computeIfAbsent(meta.name(), name -> {
					SwitchSetting category = SwitchSetting.create()
						.name(meta.name())
						.icon(meta.icon())
						.config(meta.configKey() + ".active")
						.defaultValue(true)
						.subSettings(); // creates a header

					category.getStorage().configKey = meta.configKey();
					return category;
				}
			);
		}

		return findCategory(Reflection.getParentPackage(pkg));
	}

	/**
	 * Initialises the main element and config key.
	 */
	public void init() {
		MainElementData data = SettingLoader.initMainElement(this, category.configKey());
		mainElement = data.mainElement;
		configKey = data.configKey;
	}

	/**
	 * Must happen after initialization for sorting using the main element.
	 */
	public void addToCategory() {
		category.addSetting(mainElement);
	}

	public BaseSetting<?> getMainElement() {
		return mainElement;
	}

	public SwitchSetting getCategory() {
		return category;
	}

	public String getConfigKey() {
		return configKey;
	}

	/**
	 * Checks if the parent category and the feature itself is enabled.
	 */
	public boolean isEnabled() {
		if (!category.get())
			return false;

		if (mainElement instanceof SwitchSetting)
			return ((SwitchSetting) mainElement).get();
		if (mainElement instanceof NumberSetting)
			return ((NumberSetting) mainElement).get() != 0;
		return true;
	}

	public static List<SwitchSetting> getCategories() {
		return categories.entrySet().stream()
			.filter(e -> e.getKey() != null)
			.map(Map.Entry::getValue)
			.collect(Collectors.toList());
	}

	public static List<BaseSetting<?>> getUncategorized() {
		return categories.computeIfAbsent(null, p -> SwitchSetting.create()).getChildSettings();
	}

	public static Stream<Feature> getFeatures() {
		return FileProvider.getClassesWithSuperClass(Feature.class).stream()
			.filter(m -> !m.isAbstract())
			.map(meta -> FileProvider.getSingleton(meta.load()));
	}

	@Retention(RUNTIME)
	@Target(FIELD)
	public @interface MainElement {

		boolean configureSubSettings() default true;

	}

	@Retention(RUNTIME)
	@Target(PACKAGE)
	public @interface Category {

		String name();

		String icon();

		String configKey();

	}

	@Retention(RUNTIME)
	@Target(TYPE)
	public @interface FeatureCategory {}

}
