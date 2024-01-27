/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.api.bridges;

import dev.l3g7.griefer_utils.api.file_provider.FileProvider;

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

		static String[] labyVersions = new String[]{"laby4", "laby3"};
		static String[] minecraftVersions = new String[]{"v1_8_9"};

		/**
		 * @return Whether the requested version is available.
		 */
		public static boolean init(String labyVersion, String minecraftVersion) {
			return init(labyVersion, labyVersions) && init(minecraftVersion, minecraftVersions);
		}

		private static boolean init(String target, String[] availableTargets) {
			boolean found = false;

			for (String availableTarget : availableTargets) {
				if (target.equals(availableTarget))
					found = true;
				else
					FileProvider.exclude("dev/l3g7/griefer_utils/" + availableTarget);
			}

			return found;
		}
	}

}
