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

package dev.l3g7.griefer_utils.core.misc.matrix.jna.util;

import dev.l3g7.griefer_utils.core.misc.CustomSSLSocketFactoryProvider;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

public class LibOlmLoader {

	public static String getPath() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win"))
			return "libolm";
		if (os.contains("mac"))
			return System.getProperty("java.library.path") + File.separator + "libolm.dylib";

		return null;
	}

	public static void load() throws IOException {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			// Download library
			File libFile = new File("libraries/org/matrix/libolm/2.0.3/libolm.dll");
			if (!libFile.exists()) {
				libFile.getParentFile().mkdirs();
				String url = "https://gitlab.com/famedly/company/frontend/libraries/olm/-/jobs/2995118899/artifacts/raw/libolm.dll";
				HttpsURLConnection c = (HttpsURLConnection) new URL(url).openConnection();
				c.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
				Files.copy(c.getInputStream(), libFile.toPath());
			}

			// Add library to jna.library.path
			String jnaPath = System.getProperty("jna.library.path", "");
			if (!jnaPath.isEmpty())
				jnaPath += File.pathSeparator;

			System.setProperty("jna.library.path", jnaPath + libFile.getParentFile().getAbsolutePath());
		}
		else if (os.contains("mac")) {
			File libFile = new File(System.getProperty("java.library.path"), "libolm.dylib");

			if (!libFile.exists()) {
				// Download library
				libFile.getParentFile().mkdirs();
				String url = "https://grieferutils.l3g7.dev/v2/prebuilt/libolm.3.2.15.dylib";
				HttpsURLConnection c = (HttpsURLConnection) new URL(url).openConnection();
				c.setSSLSocketFactory(CustomSSLSocketFactoryProvider.getCustomFactory());
				c.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
				Files.copy(c.getInputStream(), libFile.toPath());
			}
		}
	}
}