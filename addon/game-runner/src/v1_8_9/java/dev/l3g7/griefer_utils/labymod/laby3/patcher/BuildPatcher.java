/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.patcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.mapping.Mapper;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.util.io.IO;
import dev.l3g7.griefer_utils.core.auto_update.AutoUpdater;
import dev.l3g7.griefer_utils.labymod.laby3.Entrypoint;
import dev.l3g7.griefer_utils.labymod.laby3.Init;
import dev.l3g7.griefer_utils.labymod.laby3.patcher.build_patches.AssetsChecker;
import dev.l3g7.griefer_utils.labymod.laby3.patcher.build_patches.MappingGenerator;
import dev.l3g7.griefer_utils.labymod.laby3.patcher.build_patches.RecordConverter;
import dev.l3g7.griefer_utils.labymod.laby3.patcher.build_patches.refmap_generator.RefmapGenerator;
import dev.pymdk.mapper.Mapping;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Patches applied after building the jar file.
 */
public class BuildPatcher {

	private static final Map<String, String> LABY_3_ADDON_JSON = Map.of(
		"uuid", "%uuid%",
		"name", "GrieferUtils",
		"description", "\uD83D\uDC4B",
		"icon", "griefer_utils_icon",
		"debug", System.getProperty("griefer_utils.debug"),
		"beta", System.getProperty("griefer_utils.beta"),
		"transformerClass", dev.l3g7.griefer_utils.labymod.laby3.Init.class.getName(),
		"addonVersion", System.getProperty("griefer_utils.version")
	);

	private static final RuntimePatcher runtimePatcher = new RuntimePatcher();
	private static FileSystem fs;

	public static void main(String[] args) throws IOException {
		String version = System.getProperty("griefer_utils.version");

		// Rename jar
		File jar = new File("build/libs/GrieferUtils-release.jar");
		File newJar = new File("build/libs/griefer-utils-v" + version + ".jar");
		Files.copy(jar.toPath(), newJar.toPath(), REPLACE_EXISTING);

		// Trigger patches
		try (FileSystem fs = FileSystems.newFileSystem(newJar.toPath())) {
			BuildPatcher.fs = fs;

			Mapper.loadMappings(Paths.get("./build"), Mapping.INTERMEDIARY, false);
			RefmapGenerator.generateRefmap(fs);
			MappingGenerator.generateMappings(fs);

			mergeAddonJson();
			patchBootstrapClasses();
			RecordConverter.convertRecords(fs);
			AssetsChecker.validateAssets(fs);

			cleanup();
		}
	}

	private static Path pathOf(Class<?> c) {
		return fs.getPath(c.getName().replace('.', '/') + ".class");
	}

	/**
	 * Merges the LabyMod 3 addon.json into the existing json.
	 */
	private static void mergeAddonJson() throws IOException {
		// Read
		JsonObject addon = IO.read(fs.getPath("addon.json")).asJsonObject();

		// Merge
		LABY_3_ADDON_JSON.forEach(addon::addProperty);

		// Write
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try (OutputStream out = Files.newOutputStream(fs.getPath("addon.json"))) {
			out.write(gson.toJson(addon).getBytes(UTF_8));
		}
	}

	/**
	 * Transforms the entrypoint class and transformers.
	 */
	private static void patchBootstrapClasses() throws IOException {
		patchClass(pathOf(Init.class), true);
		patchClass(pathOf(Entrypoint.class), true);
		patchClass(pathOf(Named.class), true); // For ReleaseChannel enum
		patchClassesInFolder(pathOf(AutoUpdater.class).getParent(), true);
		patchClassesInFolder(pathOf(RuntimePatcherLoader.class).getParent(), false);
	}

	private static void patchClass(Path path, boolean checkForwardCompatibility) {
		try {
			byte[] bytes = Files.readAllBytes(path);
			String name = path.toString().substring(0, path.toString().length() - 6).replace('/', '.');
			Files.write(path, runtimePatcher.transform(name, name, bytes, checkForwardCompatibility));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void patchClassesInFolder(Path path, boolean checkForwardCompatibility) throws IOException {
		try (Stream<Path> stream = Files.walk(path)) {
			stream
				.filter(Files::isRegularFile)
				.forEach(c -> patchClass(c, checkForwardCompatibility));
		}
	}

	private static void cleanup() {
		// delete build patches
		delete(pathOf(RefmapGenerator.class).getParent());
		delete(pathOf(BuildPatcher.class));

		// empty other build artifacts to emphasize patched jar file
		empty(Paths.get("build/libs/GrieferUtils-release.jar"));
	}

	private static void delete(Path path) {
		try {
			if (!Files.exists(path))
				return;

			if (Files.isRegularFile(path)) {
				Files.delete(path);
				return;
			}

			try (Stream<Path> walk = Files.walk(path)) {
				walk.sorted(Comparator.reverseOrder())
					.filter(Predicate.not(path::equals))
					.forEach(BuildPatcher::delete);
			}
			Files.delete(path);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void empty(Path path) {
		try {
			if (!Files.exists(path))
				return;

			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			new ZipOutputStream(bout).close();
			Files.write(path, bout.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
