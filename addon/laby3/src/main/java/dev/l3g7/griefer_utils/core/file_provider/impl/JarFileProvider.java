/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.file_provider.impl;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;

import java.net.URLDecoder;
import java.util.jar.JarFile;

/**
 * An implementation for providing files from a jar file.
 */
public class JarFileProvider extends FileProvider {

	public static final JarFileProvider INSTANCE = new JarFileProvider();

	private JarFileProvider() {}

	/**
	 * Adds the content of the jar file containing the given class to the cache.
	 * @return the error if one occurred, null otherwise
	 */
	protected Throwable update0(Class<?> refClass) {
		try {
			String jarPath = refClass.getProtectionDomain().getCodeSource().getLocation().getFile();
			if (!jarPath.contains(".jar"))
				throw new IllegalStateException("Invalid code source location: " + jarPath);

			// Sanitize jarPath
			jarPath = jarPath.substring(5, jarPath.lastIndexOf("!")); // remove protocol and class
			jarPath = URLDecoder.decode(jarPath, "UTF-8");

			// Read entries
			JarFile jarFile = new JarFile(jarPath);

			if (jarFile.size() == 0)
				return new IllegalStateException("Empty jar file: " + jarPath);

			jarFile.stream().forEach(entry -> fileCache.putIfAbsent(entry.getName(), () -> jarFile.getInputStream(entry)));
		} catch (Exception e) {
			return e;
		}
		return null;
	}

}
