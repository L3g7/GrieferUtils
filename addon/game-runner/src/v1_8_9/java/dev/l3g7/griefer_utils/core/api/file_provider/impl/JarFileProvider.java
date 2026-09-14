/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.file_provider.impl;

import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.util.Util;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

/**
 * An implementation for providing files from a jar file.
 */
public class JarFileProvider extends FileProvider {

	public static final JarFileProvider INSTANCE = new JarFileProvider();

	private JarFileProvider() {}

	/**
	 * Adds the content of the jar file containing the given class to the cache.
	 *
	 * @return the error if one occurred, null otherwise
	 */
	protected @Nullable Throwable update0(Class<?> refClass) {
		String jarPath = "<uninitialized>";
		try {
			jarPath = refClass.getProtectionDomain().getCodeSource().getLocation().getFile();
			if (!jarPath.contains(".jar"))
				throw new IllegalStateException("Invalid code source location: " + jarPath);

			// Sanitize jarPath
			if (jarPath.contains("!"))
				jarPath = jarPath.substring(0, jarPath.indexOf("!"));

			Path path = Paths.get(URI.create(jarPath));

			// Read entries
			@SuppressWarnings("resource") // Keep jarFile open for InputStream suppliers
			JarFile jarFile = new JarFile(path.toFile());
			jarFile.stream().forEach(entry -> {
				if (!exclusions.contains(entry.getName()))
					fileCache.putIfAbsent(entry.getName(), () -> jarFile.getInputStream(entry));
			});

			return null;
		} catch (Exception e) {
			return Util.elevate(e, "Tried to load jar from %s", jarPath);
		}
	}

}
