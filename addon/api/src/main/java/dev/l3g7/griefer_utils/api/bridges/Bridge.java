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

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.VersionType.LABYMOD;
import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.VersionType.MINECRAFT;
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

	enum Version {

		LABY_3("laby3"), LABY_4("laby4"), ANY_LABY(null),
		MINECRAFT_1_8_9("v1_8_9", "1.8.9"), ANY_MINECRAFT(null, null);

		final VersionType type; // NOTE: access must be elevated for Java 8 to work; fix?
		final String pkg; // NOTE: access must be elevated for Java 8 to work; fix?
		public final String refmap; // Specific to Minecraft versions

		Version(String pkg) {
			this.type = LABYMOD;
			this.pkg = pkg;
			this.refmap = null;
		}

		Version(String pkg, String refmap) {
			this.type = MINECRAFT;
			this.pkg = pkg;
			this.refmap = refmap;
		}

		public static Version getMinecraftBySemVer(String semVer) {
			String version = "v" + semVer.replace('.', '_');
			for (Version value : values())
				if (value.type == MINECRAFT && version.equals(value.pkg))
					return value;

			return null;
		}

		public boolean isActive() {
			return pkg == null /* ANY */ || type.current == this;
		}

		/**
		 * @return whether the current environment is compatible with the given versions
		 */
		public static boolean isCompatible(Version[] versions) {
			// Check if at least one compatible version exists for every type
			typeLoop:
			for (VersionType type : VersionType.values()) {
				boolean typeDeclared = false; // Whether a version target was defined for this type
				for (Version version : versions)
					if (version.type == type) {
						typeDeclared = true;
						if (version.isActive())
							continue typeLoop;
					}

				// No compatible version found
				if (typeDeclared)
					return false;
			}

			return true;
		}

		public enum VersionType {
			MINECRAFT, LABYMOD;

			Version current; // NOTE: access must be elevated for Java 8 to work; fix?

			public Version getCurrent() {
				return current;
			}
		}
	}

	@Retention(RUNTIME)
	@Target(TYPE)
	@interface ExclusiveTo {

		Version[] value();

	}

}
