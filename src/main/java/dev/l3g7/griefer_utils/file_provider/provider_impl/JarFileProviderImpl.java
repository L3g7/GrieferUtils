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

package dev.l3g7.griefer_utils.file_provider.provider_impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * An implementation for providing files if the addon was loaded from a jar file.
 */
public class JarFileProviderImpl implements FileProviderImpl {

	private final Set<String> entries = new HashSet<>();
	private final JarFile jarFile;

	/**
	 * IDk what is happening here, I'm just the guy writing the docs
	 */
	public JarFileProviderImpl() throws IOException, IllegalStateException {

		String jarPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
		if (!jarPath.contains(".jar"))
			throw new IllegalStateException("Invalid code source location: " + jarPath);

		// Sanitize jarPath
		jarPath = jarPath.substring(6, jarPath.lastIndexOf("!")); // remove protocol and class
		jarPath = URLDecoder.decode(jarPath, "UTF-8");

		// Read entries
		jarFile = new JarFile(jarPath);
		jarFile.stream().forEach(entry -> entries.add(entry.getName()));

		if (entries.isEmpty())
			throw new IllegalStateException("Empty jar file: " + jarPath);
	}

	@Override
	public Collection<String> getFiles() {
		return entries;
	}

	@Override
	public InputStream getData(String file) {
		try {
			return jarFile.getInputStream(jarFile.getJarEntry(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
