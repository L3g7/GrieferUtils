/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.bridges;

import com.sun.jna.Platform;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Function;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.BooleanSupplier;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Reason.IMPLEMENTATION;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
public @interface Bridge {

	@Retention(RUNTIME)
	@Target(TYPE)
	@interface Bridged {}

	@Retention(RUNTIME)
	@Target(TYPE)
	@interface Fallback {}

	class Initializer {

		private static Version labyVersion;

		public static void init(Version labyVersion) {
			Initializer.labyVersion = labyVersion;

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

		// Laby versions
		LABY_3(v -> Initializer.labyVersion == v),
		LABY_4(v -> Initializer.labyVersion == v),

		// Operating systems
		WINDOWS(Platform::isWindows),
		LINUX(Platform::isLinux);

		private final Function<Version, Boolean> activeCheck;
		private Boolean isActive;

		Version(BooleanSupplier activeCheck) {
			this(t -> activeCheck.getAsBoolean());
		}

		Version(Function<Version, Boolean> activeCheck) {
			this.activeCheck = activeCheck;
		}

		public boolean isActive() {
			if (isActive == null)
				isActive = this.activeCheck.apply(this);

			return isActive;
		}

	}

	/**
	 * Reasons for an @ExclusiveTo annotation.
	 */
	enum Reason {
		/**
		 * The annotated class is an implementation detail (there are implementations for the other versions as well).
		 */
		IMPLEMENTATION,

		/**
		 * The annotated class is an implementation detail, but not implemented for other versions.
		 */
		NOT_IMPLEMENTED,

		/**
		 * The annotated class is not possible in other versions (due to exclusive dependencies).
		 */
		NOT_POSSIBLE,

		/**
		 * The annotated class is not needed in other versions.
		 */
		NOT_NEEDED,
	}

	@Retention(RUNTIME)
	@Target(TYPE)
	@interface ExclusiveTo {
		Version value();

		Reason reason() default IMPLEMENTATION;

		String customMessage() default "";
	}

}
