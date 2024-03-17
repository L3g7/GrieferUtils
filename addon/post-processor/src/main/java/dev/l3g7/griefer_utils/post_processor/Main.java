/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor;

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Main {

	private static final String LABY_3_FOLDER = "/dev/l3g7/griefer_utils/laby3";

	public static void main(String[] args) throws IOException {
		String version = System.getProperty("griefer_utils.version");

		// Rename jar
		File jar = new File("..\\build\\libs\\GrieferUtils-release.jar");
		File newJar = new File(jar.getParentFile(), "griefer-utils-v" + version + ".jar");
		Files.copy(jar.toPath(), newJar.toPath(), REPLACE_EXISTING);

		// Apply patches
		try (FileSystem fs = FileSystems.newFileSystem(newJar.toPath())) {
			// Merge json
			mergeAddonJson(fs);

			// Overwrite class versions
			try (Stream<Path> stream = Files.walk(fs.getPath(LABY_3_FOLDER))) {
				stream
					.filter(p -> !Files.isDirectory(p))
					.filter(p -> p.startsWith(LABY_3_FOLDER + "/PreStart.class") || (p.toString().startsWith(LABY_3_FOLDER + "/PreStart$") && p.toString().endsWith(".class")))
					.forEach(Main::overwriteClassVersion);
			}
		}
	}

	/**
	 * Adds the missing LabyMod 3 addon.json properties to existing file.
	 */
	private static void mergeAddonJson(FileSystem fs) throws IOException {
		Path addonJson = fs.getPath("/addon.json");

		JsonObject addon;
		try (Reader in = new InputStreamReader(Files.newInputStream(addonJson), UTF_8)) {
			addon = Streams.parse(new JsonReader(in)).getAsJsonObject();
		}

		// Write addon.json
		try (OutputStream out = Files.newOutputStream(addonJson)) {
			addon.addProperty("uuid", "%uuid%");
			addon.addProperty("name", "GrieferUtils");
			addon.addProperty("icon", "griefer_utils_icon");
			addon.addProperty("debug", System.getProperty("griefer_utils.debug"));
			addon.addProperty("transformerClass", "dev.l3g7.griefer_utils.laby3.PreStart");
			addon.addProperty("debug", "false"); // NOTE: don't hardcode
			addon.addProperty("addonVersion", addon.get("version").getAsString());

			out.write(addon.toString().getBytes(StandardCharsets.ISO_8859_1));
		}
	}

	/**
	 * Overwrites the class version of a class file to allow execution in Java 8.
	 */
	private static void overwriteClassVersion(Path path) {
		System.out.println("sout go brrrrrrr " + path);
		try {
			byte[] bytes = Files.readAllBytes(path);
			bytes[7 /* major_version */] = 52 /* Java 1.8 */;
			Files.write(path, bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
