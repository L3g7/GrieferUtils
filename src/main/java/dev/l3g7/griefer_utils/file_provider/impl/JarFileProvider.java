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

package dev.l3g7.griefer_utils.file_provider.impl;

import dev.l3g7.griefer_utils.file_provider.FileProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.NoSuchFileException;
import java.util.Collection;
import java.util.HashMap;
import java.util.jar.JarFile;

import static dev.l3g7.griefer_utils.util.Util.elevate;

/**
 * An implementation for providing files if the addon was loaded from a jar file.
 */
public class JarFileProvider extends FileProvider {

	public static final JarFileProvider INSTANCE = new JarFileProvider();

	private final HashMap<String, JarFile> entries = new HashMap<>();

	private JarFileProvider() {}

	/**
	 * Loads the jar file containing the passed class.
	 * @return the error if one occurred, null otherwise
	 */
	@Override
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
			jarFile.stream().forEach(entry -> entries.put(entry.getName(), jarFile));

			if (jarFile.size() == 0)
				return new IllegalStateException("Empty jar file: " + jarPath);

		} catch (Exception e) {
			return e;
		}
		return null;
	}

	/**
	 * @return a list of all known files.
	 */
	@Override
	protected Collection<String> getFiles0() {
		return entries.keySet();
	}

	/**
	 * @return an InputStream containing the given file's contents
	 */
	@Override
	protected InputStream getData0(String file) {
		if (!entries.containsKey(file))
			throw elevate(new NoSuchFileException(file));

		try {
			JarFile jarFile = entries.get(file);
			return jarFile.getInputStream(jarFile.getJarEntry(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
