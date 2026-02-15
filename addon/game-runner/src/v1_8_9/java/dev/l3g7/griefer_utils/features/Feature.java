/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features;

import dev.l3g7.griefer_utils.core.api.event_bus.Disableable;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.misc.functions.Runnable;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.settings.AbstractSetting;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.GUIEntry;
import dev.l3g7.griefer_utils.core.settings.SettingLoader;
import dev.l3g7.griefer_utils.core.settings.SettingLoader.MainElementData;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.features._dyn_ght.GUIHierarchyTree;
import dev.l3g7.griefer_utils.features._dyn_ght.Lazy;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The base class for features.
 */
public abstract class Feature implements Disableable, GUIEntry {

	// Name to setting
	private static final Map<String, CategoryData> categories = new HashMap<>();

	private final Lazy<CategoryData> category = new Lazy<>(() -> findCategory(getClass().getPackage()));
	private BaseSetting<?> mainElement;
	private String configKey;

	private CategoryData findCategory(Package pkg) {
		if (pkg == null)
			return null;

		Field[] mainElementFields = Reflection.getAnnotatedFields(this.getClass(), MainElement.class, true);
		Field mainElementField = mainElementFields[0];
		BaseSetting<?> mainElement = Reflection.get(this, mainElementField);

		String name = mainElement.name();
		GUIHierarchyTree.Feat feat = GUIHierarchyTree.get(name);
		mainElement.name(feat.name())
			.icon(feat.icon());

		return build(feat.parent());
	}

	private CategoryData build(GUIHierarchyTree.Feat feat) {
		if (feat == null)
			return null;

		CategoryData cur = categories.get(feat.name());
		if (cur != null)
			return cur;

		CategoryData parent = build(feat.parent());

		String configKey = feat.name();
		if (parent != null)
			configKey = "temp_settings." + configKey;

		BaseSetting<?> category = FileProvider.getSingleton(feat.setting())
			.build(new Category() {
				@Override
				public String name() {
					return feat.name();
				}

				@Override
				public String description() {
					return feat.description();
				}

				@Override
				public String icon() {
					return (String) feat.icon();
				}

				@Override
				public Class<? extends SettingBuilder> setting() {
					return feat.setting();
				}

				@Override
				public Class<? extends Annotation> annotationType() {
					return Category.class;
				}
			}, configKey);

		CategoryData data = new CategoryData(category, configKey, parent);
		categories.put(feat.name(), data);
		return data;
	}

	/**
	 * Initialises the main element and config key.
	 */
	public void init() {
		MainElementData data = SettingLoader.initMainElement(this, category.get() == null ? null : category.get().configKey(), getConfigSubkey());
		mainElement = data.mainElement;
		configKey = data.configKey;
	}

	protected String getConfigSubkey() {
		return SettingLoader.getDefaultConfigSubkey(this);
	}

	@Override
	public String name() {
		return mainElement.name();
	}

	@Override
	public void addToParent(List<BaseSetting<?>> root) {
		if (category.get() != null)
			category.get().getSetting().addSetting(mainElement);
	}

	public BaseSetting<?> getMainElement() {
		return mainElement;
	}

	public CategoryData getCategory() {
		return category.get();
	}

	public String getConfigKey() {
		return configKey;
	}

	/**
	 * Checks if the parent category and the feature itself is enabled.
	 */
	public boolean isEnabled() {
		if (category.get() != null && !category.get().isEnabled())
			return false;

		if (mainElement instanceof SwitchSetting)
			return ((SwitchSetting) mainElement).get();
		if (mainElement instanceof NumberSetting)
			return ((NumberSetting) mainElement).get() != 0;
		return true;
	}

	public static List<CategoryData> getCategories() {
		return categories.entrySet().stream()
			.filter(e -> e.getKey() != null)
			.map(Map.Entry::getValue)
			.collect(Collectors.toList());
	}

	public static List<BaseSetting<?>> getUncategorized() {
		return getFeatures().filter(f -> f.category.get() == null)
			.map(f -> f.mainElement)
			.collect(Collectors.toList());
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

		String description() default "";

		String icon();

		Class<? extends SettingBuilder> setting() default SwitchSettingBuilder.class;

	}

	public static final class CategoryData implements GUIEntry {

		private final BaseSetting<?> setting;
		private final String configKey;
		private final CategoryData parent;

		private CategoryData(BaseSetting<?> setting, String configKey, CategoryData parent) {
			this.setting = setting;
			this.configKey = configKey;
			this.parent = parent;
		}

		public String configKey() {
			return configKey;
		}

		public BaseSetting<?> getSetting() {
			return setting;
		}

		public boolean isEnabled() {
			if (parent != null && !parent.isEnabled())
				return false;

			if (setting instanceof SwitchSetting s)
				return s.get();
			if (setting instanceof NumberSetting s)
				return s.get() != 0;
			return true;
		}

		@Override
		public String name() {
			return setting.name();
		}

		@Override
		public void addToParent(List<BaseSetting<?>> root) {
			if (parent != null)
				parent.getSetting().addSetting(setting);
			else
				root.add(setting);
		}

		public void callback(Runnable callback) {
			if (setting instanceof AbstractSetting<?,?> as)
				as.callback(callback);
			if (parent != null)
				parent.callback(callback);
		}

		public void callback(Consumer<Boolean> callback) {
			if (setting instanceof SwitchSetting s)
				s.callback(callback);
			if (parent != null)
				parent.callback(callback);
		}

	}

}
