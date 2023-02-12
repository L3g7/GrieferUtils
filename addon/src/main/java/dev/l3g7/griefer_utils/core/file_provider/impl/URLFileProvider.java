/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.core.file_provider.impl;

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;

import static dev.l3g7.griefer_utils.core.util.Util.addMessage;

/**
 * An implementation for providing files loaded using an URLClassLoader.
 */
public class URLFileProvider extends FileProvider {

	public static final URLFileProvider INSTANCE = new URLFileProvider();

	private URLFileProvider() {}

	/**
	 * Adds all files known by the system class loader to the cache.
	 * @return the error if one occurred, null otherwise
	 */
	@Override
	protected Throwable update0(Class<?> ignored) {
		for (URL url : ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs()) {
			try {
				File root = new File(url.toURI());
				load(root, root);
			} catch (Exception e) {
				return addMessage(e, "Tried to load urls from " + ClassLoader.getSystemClassLoader());
			}
		}
		return null;
	}

	/**
	 * Loads the given file.
	 */
	private void load(File root, File file) throws IOException {
		if (file.isDirectory())
			for (File child : file.listFiles())
				load(root, child);

		else if (file != root) {
			// Strip root path and normalize string
			String path = file.getCanonicalPath().substring(root.getCanonicalPath().length() + 1).replace('\\', '/');
			fileCache.put(path, () -> Files.newInputStream(file.toPath()));
		}
	}

}
