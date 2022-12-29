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

import com.google.common.collect.ImmutableList;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A feature category.
 */
public class Category {

	private static final Map<Package, Category> categoryCache = new HashMap<>();

	private final BooleanSetting setting;
	private final String configKey;

	/**
	 * Creates a new category from the given package, using data from its meta annotation.
	 */
	private Category(Package pkg) {
		if (pkg.isAnnotationPresent(Uncategorized.class)) {
			setting = new BooleanSetting().set(true);
			configKey = null;
			return;
		}

		if (!pkg.isAnnotationPresent(Meta.class))
			throw new IllegalStateException("Could not find category of " + pkg);

		Meta meta = pkg.getDeclaredAnnotation(Meta.class);
		configKey = meta.configKey();

		setting = new BooleanSetting()
			.name(meta.name())
			.icon(meta.icon())
			.config(configKey + ".active")
			.defaultValue(true)
			.subSettings();
	}

	public static Category getCategory(Package pkg) {
		return categoryCache.computeIfAbsent(pkg, Category::new);
	}

	public static Collection<Category> getCategories() {
		return categoryCache.values();
	}

	public BooleanSetting getSetting() {
		return setting;
	}

	public String getConfigKey() {
		return configKey;
	}

	public boolean isEnabled() {
		return setting.get();
	}

	public void add(Feature feature) {
		setting.subSettings(ImmutableList.of(feature.getMainElement()));
	}

	/**
	 * An annotation for defining the metadata of a category.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PACKAGE)
	public @interface Meta {

		String name();
		String icon();
		String configKey();

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PACKAGE)
	public @interface Uncategorized {}
}
