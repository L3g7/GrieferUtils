/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.bridges;

import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
public @interface Bridge {

	@Retention(RUNTIME)
	@Target(TYPE)
	@interface Bridged {}

	class Initializer {

		public static void init(Version labyVersion) {
			Version.current = labyVersion;

			// Exclude incompatible versions
			for (Version value : Version.values())
				if (!value.isActive())
					FileProvider.exclude("dev/l3g7/griefer_utils/" + value.name().toLowerCase().replace("_", ""));

			// Remove incompatible files
			FileProvider.exclude(m -> {
				if (!m.hasAnnotation(ExclusiveTo.class))
					return false;

				Version version = m.getAnnotation(ExclusiveTo.class).getValue("value", true);
				return !version.isActive();
			});
		}

	}

	enum Version {

		LABY_3, LABY_4;

		private static Version current;

		public boolean isActive() {
			return current == this;
		}

	}

	@Retention(RUNTIME)
	@Target(TYPE)
	@interface ExclusiveTo {
		Version value();
	}

}
