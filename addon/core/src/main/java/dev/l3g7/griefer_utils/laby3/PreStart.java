/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby3;

import com.google.gson.JsonObject;
import net.labymod.addon.AddonLoader;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class PreStart implements IClassTransformer {

	@SuppressWarnings("unchecked")
	public PreStart() throws Exception {
		System.out.println("Laby3 Transformer loaded!");


		Field f = AddonLoader.class.getDeclaredField("mainClasses");
		f.setAccessible(true);
		Map<UUID, String> mainClasses = (Map<UUID, String>) f.get(null);

		Optional<Map.Entry<UUID, String>> entry = mainClasses.entrySet().stream()
			.filter(e -> e.getValue().startsWith("dev.l3g7.griefer_utils."))
			.findAny();

		UUID uuid = entry.isPresent() ? entry.get().getKey() : UUID.randomUUID();

		// Overwrite main class
		mainClasses.put(uuid, "dev.l3g7.griefer_utils.Main");

		// Overwrite version
		f = AddonLoader.class.getDeclaredField("loadedOffline");
		f.setAccessible(true);
		Map<UUID, JsonObject> loadedOffline = (Map<UUID, JsonObject>) f.get(null);
		loadedOffline.get(uuid).addProperty("version", 1);

		// The following code is temporary, it will be removed once the two branches (labymod-4 and v2) are merged successfully

		File ownJar = getOwnJar();
		File targetFile = Files.createTempFile("griefer_utils_labymod_3", ".gu_jar").toFile();
		Runtime.getRuntime().addShutdownHook(new Thread(targetFile::delete));

		extractJar(ownJar, targetFile);
		addToClassLoaders(targetFile.toURI().toURL());
		removeURLFromClassLoaders(ownJar.toURI().toURL());

		System.setProperty("griefer_utils_loader_jar", ownJar.getAbsolutePath());
		Class.forName("dev.l3g7.griefer_utils.PreStart").getDeclaredConstructor().newInstance();
	}

	private File getOwnJar() throws UnsupportedEncodingException {
		String jarPath = PreStart.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		if (!jarPath.contains(".jar"))
			throw new IllegalStateException("Invalid code source location: " + jarPath);

		// Sanitize jarPath
		jarPath = jarPath.substring(5, jarPath.lastIndexOf("!")); // remove protocol and class
		jarPath = URLDecoder.decode(jarPath, "UTF-8");

		return new File(jarPath);
	}

	private void extractJar(File container, File targetFile) throws IOException {
		// Read entries
		try (JarFile jarFile = new JarFile(container);
		     OutputStream out = new FileOutputStream(targetFile)) {

			ZipEntry entry = jarFile.getEntry("griefer_utils_labymod_3.jar");
			InputStream in = jarFile.getInputStream(entry);

			// Extract file
			byte[] buf = new byte[8192];
			for (int l; (l = in.read(buf)) != -1; )
				out.write(buf, 0, l);
		}
	}

	private void addToClassLoaders(URL url) throws ReflectiveOperationException {
		Field parentField = LaunchClassLoader.class.getDeclaredField("parent");
		parentField.setAccessible(true);

		Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		addUrlMethod.setAccessible(true);

		for (URLClassLoader classLoader : new URLClassLoader[] {Launch.classLoader, (URLClassLoader) parentField.get(Launch.classLoader)}) {
			addUrlMethod.invoke(classLoader, url);
		}
	}

	/**
	 * Removes the given URL from the LaunchClassLoader and its parent.
	 */
	@SuppressWarnings("unchecked")
	private static void removeURLFromClassLoaders(URL urlToRemove) throws ReflectiveOperationException, IOException {
		// Create fields for access to stores
		Class<?> urlClassPathClass = Class.forName("sun.misc.URLClassPath");
		Field ucpField = URLClassLoader.class.getDeclaredField("ucp");
		ucpField.setAccessible(true);
		Field pathField = urlClassPathClass.getDeclaredField("path");
		pathField.setAccessible(true);
		Field lmapField = urlClassPathClass.getDeclaredField("lmap");
		lmapField.setAccessible(true);
		Field loadersField = urlClassPathClass.getDeclaredField("loaders");
		loadersField.setAccessible(true);

		Field parentField = LaunchClassLoader.class.getDeclaredField("parent");
		parentField.setAccessible(true);
		for (URLClassLoader classLoader : new URLClassLoader[] {Launch.classLoader, (URLClassLoader) parentField.get(Launch.classLoader)}) {
			// Extract stores
			Object ucp = ucpField.get(classLoader);
			ArrayList<URL> path = (ArrayList<URL>) pathField.get(ucp);
			HashMap<String, Object> lmap = (HashMap<String, Object>) lmapField.get(ucp);
			ArrayList<Object> loaders = (ArrayList<Object>) loadersField.get(ucp);

			// Remove old URL
			path.remove(urlToRemove);
			Object loader = lmap.remove(String.format("file://%s", urlToRemove.getFile()));
			if (loader == null)
				continue;
			loaders.remove(loader);
			((Closeable) loader).close();
		}

		// Disable all lookup caches
		Method disableAllLookupCaches = urlClassPathClass.getDeclaredMethod("disableAllLookupCaches");
		disableAllLookupCaches.setAccessible(true);
		disableAllLookupCaches.invoke(null);
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) { return basicClass; }

}
