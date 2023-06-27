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

package dev.l3g7.griefer_utils.core.misc;

import dev.l3g7.griefer_utils.core.misc.matrix.jna.util.LibOlmLoader;
import net.minecraft.launchwrapper.Launch;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;

public class LibLoader {

	private static final ClassLoader launchClassLoaderParent;
	private static final Method addURL;

	static {
		// get parent of Launch.classLoader
		try {
			Field parent = Launch.classLoader.getClass().getDeclaredField("parent");
			parent.setAccessible(true);
			launchClassLoaderParent = (ClassLoader) parent.get(Launch.classLoader);

			addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			addURL.setAccessible(true);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public static void loadLibraries() throws ReflectiveOperationException, IOException {

		// LibOlm: for the Olm ratchet (Matrix)
		LibOlmLoader.load();

		// EdDSA: for ed25519 signatures (Matrix)
		LibLoader.loadLibrary("net/i2p/crypto/eddsa/0.3.0/eddsa-0.3.0.jar", "https://repo1.maven.org/maven2/net/i2p/crypto/eddsa/0.3.0/eddsa-0.3.0.jar");

		// HKDF: for ssss decryption and password derivation (Matrix)
		LibLoader.loadLibrary("at/favre/lib/hkdf/1.1.0/hkdf-1.1.0.jar", "https://repo1.maven.org/maven2/at/favre/lib/hkdf/1.1.0/hkdf-1.1.0.jar");

		// mXparser: for evaluating expressions (Calculator)
		loadLibrary("org/mariuszgromada/math/MathParser.org-mXparser/5.1.0/MathParser.org-mXparser-5.1.0.jar", "https://repo1.maven.org/maven2/org/mariuszgromada/math/MathParser.org-mXparser/5.1.0/MathParser.org-mXparser-5.1.0.jar");

		// ZXing: for reading qr codes (QRCodeScanner)
		LibLoader.loadLibrary("com/google/zxing/core/3.5.1/core-3.5.1.jar", "https://repo1.maven.org/maven2/com/google/zxing/core/3.5.1/core-3.5.1.jar");

		// Mixin: for modifying other classes (see core.injection.mixin package)
		LibLoader.loadLibrary("org/spongepowered/mixin/0.7.11/mixin-0.7.11.jar", "https://repo.spongepowered.org/repository/maven-public/org/spongepowered/mixin/0.7.11-SNAPSHOT/mixin-0.7.11-20180703.121122-1.jar");
	}

	private static void loadLibrary(String path, String downloadUrl) throws IOException, ReflectiveOperationException {
		File libFile = new File("libraries", path);
		if (!libFile.exists()) {
			// Download library
			libFile.getParentFile().mkdirs();
			HttpsURLConnection c = (HttpsURLConnection) new URL(downloadUrl).openConnection();
			c.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
			Files.copy(c.getInputStream(), libFile.toPath());
		}

		// Add jar file to parent of LaunchClassLoader
		addURL.invoke(launchClassLoaderParent, libFile.toURI().toURL());
		addURL.invoke(Launch.classLoader, libFile.toURI().toURL());
	}

}