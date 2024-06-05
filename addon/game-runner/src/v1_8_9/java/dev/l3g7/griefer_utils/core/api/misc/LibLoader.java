/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc;

import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.api.util.StringUtil;
import dev.l3g7.griefer_utils.core.api.util.Util;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static dev.l3g7.griefer_utils.core.api.bridges.MinecraftBridge.minecraftBridge;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class LibLoader {

	private static final ClassLoader launchClassLoaderParent = Reflection.get(minecraftBridge.launchClassLoader(), "parent");

	public static void loadLibraries(String... libraries) {
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

			try {
				loadLibrary(path, url, hash);
			} catch (Throwable e) {
				throw Util.elevate(e, "Could not load library %s/%s!", group, name);
			}
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

		// Add jar file to LaunchClassLoader
		if (launchClassLoaderParent instanceof URLClassLoader)
			Reflection.invoke(launchClassLoaderParent, "addURL", libFile.toURI().toURL());

		Reflection.invoke(minecraftBridge.launchClassLoader(), "addURL", libFile.toURI().toURL());
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