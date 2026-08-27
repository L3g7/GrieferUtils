/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc;

import dev.l3g7.griefer_utils.core.api.reflection.Access;
import dev.l3g7.griefer_utils.core.api.util.Util;
import net.minecraft.launchwrapper.Launch;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.l3g7.griefer_utils.core.api.util.CryptUtil.checkHashFail;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Helper class for downloading files from Maven repositories and injecting them into the class loader.
 */
public class LibLoader {

	private static final ClassLoader launchClassLoaderParent = Util.staticInit(() -> {
		Field field = resolveField(Launch.classLoader, "parent");
		if (field == null)
			field = resolveField(Launch.classLoader, "appClassLoader");

		return (ClassLoader) field.get(Launch.classLoader);
	});

	/**
	 * Reflection.findField can't be used since it requires the LibLoader (Reflection -> Mapper -> MappingDownloder -> LibLoader)
	 */
	private static Field resolveField(Object target, String name) {
		Class<?> currentClass = target instanceof Class<?> ? (Class<?>) target : target.getClass();

		do {
			try {
				Field field = currentClass.getDeclaredField(name);
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException ignored) {
				currentClass = currentClass.getSuperclass();
			}
		} while (currentClass != null);

		return null;
	}

	/**
	 * @param hash base64-encoded SHA256
	 */
	public static void loadLibrary(String repository, String group, String name, String version, String hash) {
		loadLibrary(repository, group, name, version, null, hash);
	}

	/**
	 * @param hash base64-encoded SHA256
	 */
	public static void loadLibrary(String repository, String group, String name, String version, String classifier, String hash) {
		try {
			Path file = fetchFromMaven(repository, group, name, version, classifier, "jar", hash);
			URL url = file.toUri().toURL();

			MethodHandle access = Access.getElevatedLookup()
				.findVirtual(URLClassLoader.class, "addURL", MethodType.methodType(void.class, URL.class));

			// Add jar file to LaunchClassLoader
			if (launchClassLoaderParent instanceof URLClassLoader)
				access.invoke(launchClassLoaderParent, url);

			access.invoke(Launch.classLoader, url);
		} catch (Throwable e) {
			throw Util.elevate(e, "Could not load library %s/%s!", group, name);
		}
	}

	/**
	 * @param hash base64-encoded SHA256
	 */
	public static Path fetchFromMaven(String repository, String group, String name, String version, @Nullable String classifier, String extension, String hash) throws IOException {
		String semverVersion = version.replaceAll("^(\\d+\\.\\d+\\.\\d+)\\D.*$", "$1");
		String filePath = group + "/" + name + "/" + semverVersion + "/" + name + "-" + semverVersion + ".jar";

		String classifierApx = classifier == null ? "" : "-" + classifier;
		String url = repository + "/" + group + "/" + name + "/" + version + "/" + name + "-" + semverVersion + classifierApx + "." + extension;

		Path libPath = Launch.assetsDir.toPath().resolve("../libraries/" + filePath);
		if (!Files.exists(libPath) || checkHashFail(libPath, hash)) {
			// Download library
			Files.createDirectories(libPath.getParent());
			URLConnection c = URI.create(url).toURL().openConnection();
			c.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
			try (InputStream in = c.getInputStream()) {
				Files.copy(in, libPath, REPLACE_EXISTING);
			}

			if (checkHashFail(libPath, hash))
				// Downloading failed
				throw new IOException("File " + filePath + " has an invalid hash!");
		}

		return libPath;
	}

}