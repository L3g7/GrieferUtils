/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.api.misc;

import dev.l3g7.griefer_utils.api.util.StringUtil;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static dev.l3g7.griefer_utils.api.bridges.MinecraftBridge.minecraftBridge;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class LibLoader {

	private static final ClassLoader launchClassLoaderParent;
	private static MethodHandle addURL;

	private static final MethodHandles.Lookup globalLookup = MethodHandles.lookup();

	static {
		// get parent of Launch.classLoader
		try {
			Field parent = minecraftBridge.launchClassLoader().getClass().getDeclaredField("parent");
			parent.setAccessible(true);
			launchClassLoaderParent = (ClassLoader) parent.get(minecraftBridge.launchClassLoader());

			// MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(URLClassLoader.class, globalLookup);
			//TODO: addURL = lookup.findVirtual(URLClassLoader.class, "addURL", MethodType.methodType(void.class, URL.class)); // void addURL(URL
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public static void loadLibraries(String... libraries) throws Throwable {
		for (int i = 0; i < libraries.length; i += 5) {
			boolean hasMvnName = !libraries[i + 4].matches("^[A-F\\d]{64}$");

			String repo = libraries[i];
			String group = libraries[i + 1];
			String name = libraries[i + 2];
			String version = libraries[i + 3];
			String fileName = hasMvnName ? libraries[i + 4] : name + "-" + version + ".jar";
			String hash = libraries[hasMvnName ? ++i + 4 : i + 4];

			String cleanVersion = version.replaceAll("^(\\d+\\.\\d+\\.\\d+).+$", "$1");
			String path = group + "/" + name + "/" + cleanVersion + "/" + name + "-" + cleanVersion + ".jar";
			String url = repo + "/" + group + "/" + name + "/" + version + "/" + fileName;

			loadLibrary(path, url, hash);
		}
	}

	private static void loadLibrary(String path, String downloadUrl, String hash) throws Throwable {
		File libFile = new File(minecraftBridge.assetsDir(), "../libraries/" + path);
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
		// TODO: module does not open shit
		// addURL.invoke(launchClassLoaderParent, libFile.toURI().toURL());
		// addURL.invoke(minecraftBridge.launchClassLoader(), libFile.toURI().toURL());
	}

	private static boolean verifyHash(File libFile, String targetHash) throws IOException {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] fileHash = md.digest(Files.readAllBytes(libFile.toPath()));
			return Arrays.equals(fileHash, StringUtil.decodeHex(targetHash));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}