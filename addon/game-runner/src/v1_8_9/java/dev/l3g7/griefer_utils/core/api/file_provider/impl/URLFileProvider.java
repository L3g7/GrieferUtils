/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.file_provider.impl;

import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * An implementation for providing files loaded using an URLClassLoader.
 */
public class URLFileProvider extends FileProvider {

	public static final URLFileProvider INSTANCE = new URLFileProvider();

	private URLFileProvider() {}

	/**
	 * Adds all files known by the system class loader to the cache.
	 *
	 * @return the error if one occurred, null otherwise
	 */
	@Override
	protected @Nullable Throwable update0(Class<?> refClass) {
		for (URL url : ((URLClassLoader) refClass.getClassLoader()).getURLs()) {
			try {
				Path root = Paths.get(url.toURI());
				if (!Files.exists(root))
					continue;

				try (Stream<@NotNull Path> stream = Files.walk(root)) {
					stream.forEach(entry -> {
						if (Files.isRegularFile(entry)) {
							// Strip root path and normalize string
							String path = root.relativize(entry).toString().replace('\\', '/');
							if (!exclusions.contains(path))
								fileCache.putIfAbsent(path, () -> Files.newInputStream(entry));

						}
					});
				}
			} catch (Exception e) {
				return Util.elevate(e, "Tried to load urls from %s", ClassLoader.getSystemClassLoader());
			}
		}
		return null;
	}

}
