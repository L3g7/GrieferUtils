/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.patcher.build_patches;

import dev.l3g7.griefer_utils.core.api.util.io.IO;
import dev.pymdk.mapper.impl.helpers.ClassScanner;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Checks if all assets are used and credited.
 */
@SuppressWarnings("resource")
public class AssetsChecker {

	private static final List<String> KNOWN_DIRECTORIES = Arrays.asList("litematica", "mob_icons", "biomes", "structures", "high_res");
	private static final List<String> KNOWN_FILES = Arrays.asList("pencil", "gray_sword", "diamond_sword", "menu_point");

	public static void validateAssets(FileSystem fs) throws IOException {
		List<String> directories = new ArrayList<>();
		List<String> files = new ArrayList<>();

		Files.list(fs.getPath("assets", "griefer_utils", "icons")).forEach(path -> {
			if (Files.isDirectory(path))
				directories.add(path.getFileName().toString());
			else
				files.add(path.getFileName().toString());
		});

		// Known directories and files
		files.remove("README.md");
		for (String knownDirectory : KNOWN_DIRECTORIES)
			directories.remove(knownDirectory);

		checkCredits(fs, directories, files);
		checkUnused(fs, files);
	}

	/**
	 * Checks whether all icon assets are credited.
	 */
	private static void checkCredits(FileSystem fs, List<String> directories, List<String> assetFiles) throws IOException {
		List<String> files = new ArrayList<>(assetFiles);
		String data = Files.readString(fs.getPath("assets", "griefer_utils", "icons", "README.md"));
		for (String entry : data.substring(27, data.length() - 10).split("<tr>")) {
			entry = entry.replace("</tr>", "").replace("</td>", "").trim();
			if (entry.isEmpty())
				continue;

			String name = entry.split("<td>")[1].split("<td>")[0].trim().split("<a")[1].split(">")[1].split("<")[0].trim();
			if (!KNOWN_DIRECTORIES.contains(name.split("/")[0])) {
				if (name.endsWith("/*")) {
					if (!directories.remove(name.substring(0, name.length() - 2)))
						throw new IllegalStateException("Missing directory for " + name);
				} else {
					if (!files.remove(name))
						throw new IllegalStateException("Missing file for " + name);
				}
			}
		}

		if (!directories.isEmpty() || !files.isEmpty())
			throw new IllegalStateException("Missing credits for " + directories + " / " + files);
	}

	/**
	 * Checks whether all icon assets are used.
	 */
	private static void checkUnused(FileSystem fs, List<String> assetFiles) throws IOException {
		List<String> files = new ArrayList<>(assetFiles.stream().map(f -> f.substring(0, f.length() - 4)).toList());
		for (String knownFile : KNOWN_FILES)
			files.remove(knownFile);

		Files.walk(fs.getPath("dev")).forEach(path -> {
			if (Files.isDirectory(path))
				return;

			if (!path.getFileName().toString().endsWith(".class"))
				return;

			byte[] content = IO.read(path).asBytes();
			files.removeAll(ClassScanner.getStrings(content));
		});

		if (!files.isEmpty())
			throw new IllegalStateException("Unused " + files);
	}

}
