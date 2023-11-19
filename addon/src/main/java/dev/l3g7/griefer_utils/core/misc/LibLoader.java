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

import net.minecraft.launchwrapper.Launch;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

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

		// mXparser: for evaluating expressions (Calculator)
		loadLibrary("org/mariuszgromada/math/MathParser.org-mXparser/5.1.0/MathParser.org-mXparser-5.1.0.jar", "https://repo1.maven.org/maven2/org/mariuszgromada/math/MathParser.org-mXparser/5.1.0/MathParser.org-mXparser-5.1.0.jar", "B5472B5E1BBEFEA2DA6052C68A509C84C7F2CA5F99B76A4C5F58354C08818630");

		// ZXing: for reading qr codes (QRCodeScanner)
		loadLibrary("com/google/zxing/core/3.5.1/core-3.5.1.jar", "https://repo1.maven.org/maven2/com/google/zxing/core/3.5.1/core-3.5.1.jar", "1BA7C0FBB6C267E2FB74E1497D855ADAE633CCC98EDC8C75163AA64BC08E3059");

		// Mixin: for modifying other classes (see core.injection.mixin package)
		loadLibrary("org/spongepowered/mixin/0.7.11/mixin-0.7.11.jar", "https://repo.spongepowered.org/repository/maven-public/org/spongepowered/mixin/0.7.11-SNAPSHOT/mixin-0.7.11-20180703.121122-1.jar", "DA3D6E47B9C12B5A312D89B67BC27E2429D823C09CDE8A90299E9FDCC4EEFC20");
	}

	private static void loadLibrary(String path, String downloadUrl, String hash) throws IOException, ReflectiveOperationException {
		File libFile = new File(Launch.assetsDir, "../libraries/" + path);
		if (!libFile.exists() || !verifyHash(libFile, hash)) {
			// Download library
			libFile.getParentFile().mkdirs();
			HttpsURLConnection c = (HttpsURLConnection) new URL(downloadUrl).openConnection();
			c.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
			Files.copy(c.getInputStream(), libFile.toPath(), REPLACE_EXISTING);

			if (!verifyHash(libFile, hash)) {
				// Downloading failed
				throw new IOException("File " + path + " has an invalid hash!");
			}
		}

		// Add jar file to parent of LaunchClassLoader
		addURL.invoke(launchClassLoaderParent, libFile.toURI().toURL());
		addURL.invoke(Launch.classLoader, libFile.toURI().toURL());
	}

	private static boolean verifyHash(File libFile, String targetHash) throws IOException {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] fileHash = md.digest(Files.readAllBytes(libFile.toPath()));
			return Arrays.equals(fileHash, Hex.decodeHex(targetHash.toCharArray()));
		} catch (NoSuchAlgorithmException | DecoderException e) {
			throw new RuntimeException(e);
		}
	}

}