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

import static dev.l3g7.griefer_utils.api.bridges.Bridge.VersionType.LABYMOD;
import static dev.l3g7.griefer_utils.api.bridges.Bridge.VersionType.MINECRAFT;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
public @interface Bridge {

	@Retention(RUNTIME)
	@Target(TYPE)
	@interface Bridged {}

	class Initializer {

		public static void init(Version labyVersion, Version minecraftVersion) {
			if (labyVersion.type != LABYMOD || minecraftVersion.type != MINECRAFT)
				throw new IllegalArgumentException();

			LABYMOD.current = labyVersion;
			MINECRAFT.current = minecraftVersion;

			// Exclude incompatible versions
			for (Version value : Version.values())
				if (!value.isActive())
					FileProvider.exclude("dev/l3g7/griefer_utils/" + value.pkg);

			// Remove incompatible files
			FileProvider.exclude(m -> {
				if (!m.hasAnnotation(ExclusiveTo.class))
					return false;

				Version[] versions = m.getAnnotation(ExclusiveTo.class).getValue("value", true);
				return !Version.isCompatible(versions);
			});
		}

	}

	enum VersionType {
		MINECRAFT, LABYMOD;

		private Version current;
	}

	enum Version {

		LABY_3(LABYMOD, "laby3"), LABY_4(LABYMOD, "laby4"), ANY_LABY(LABYMOD, null),
		MINECRAFT_1_8_9(MINECRAFT, "v1_8_9"), ANY_MINECRAFT(MINECRAFT, null);

		private final VersionType type;
		private final String pkg;

		Version(VersionType type, String pkg) {
			this.type = type;
			this.pkg = pkg;
		}

		public static Version getMinecraftBySemVer(String semVer) {
			String version = "v" + semVer.replace('.', '_');
			for (Version value : values())
				if (value.type == MINECRAFT && version.equals(value.pkg))
					return value;

			return null;
		}

		public boolean isActive() {
			return pkg == null || type.current == this;
		}

		/**
		 * @return whether the current environment is incompatible with the given versions
		 */
		public static boolean isCompatible(Version[] versions) {
			// Check if at least one compatible version exists for every type
			typeLoop:
			for (VersionType type : VersionType.values()) {
				for (Version version : versions)
					if (version.type == type && version.isActive())
						continue typeLoop;

				// No compatible version found
				return false;
			}

			return true;
		}

	}

	@Retention(RUNTIME)
	@Target(TYPE)
	@interface ExclusiveTo {

		Version[] value();

	}

}
