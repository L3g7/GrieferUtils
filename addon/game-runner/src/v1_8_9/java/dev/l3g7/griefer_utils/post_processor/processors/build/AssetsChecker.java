/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.build;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("resource")
public class AssetsChecker {

	private static final List<String> KNOWN_DIRECTORIES = Arrays.asList("litematica", "mob_icons", "griefer_info", "biomes", "structures");
	private static final List<String> KNOWN_FILES = Arrays.asList("thonk", "pencil", "lens", "earth");

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
				}
				else {
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

			try {
				// Decode content pool and check CONSTANT_Utf8_info entries
				byte[] content = Files.readAllBytes(path);
				short poolCount = (short) (((content[8] & 0xFF) << 8) | (content[9] & 0xFF));
				int[] startIndices = new int[poolCount];
				int cursor = 10;
				for (int idx = 0; idx < poolCount; idx++) {
					startIndices[idx] = cursor;
					byte b = content[cursor++];
					if (b == 1) { // CONSTANT_UTF8
						short length = (short) (((content[cursor++] & 0xFF) << 8) | (content[cursor++] & 0xFF));
						cursor += length;
					} else if (b == 8) { // CONSTANT_String
						short index = (short) (((content[cursor++] & 0xFF) << 8) | (content[cursor++] & 0xFF));
						int start = startIndices[index - 1] + 1;
						short length = (short) (((content[start++] & 0xFF) << 8) | (content[start++] & 0xFF));
						String data = new String(content, start, length);
						for (int i = 0; i < files.size(); i++)
							if (files.remove(data))
								break;
					}
					else if (b == 5 || b == 6) {
						// CONSTANT_Long_info / CONSTANT_Double_info take two entries
						cursor += 8;
						idx += 1;
					}
					else if (b == 7 || b == 16 || b == 19 || b == 20)
						cursor += 2;
					else if (b == 15)
						cursor += 3;
					else if (b == 3 || b == 4 || b == 9 || b == 10 || b == 11 || b == 12 || b == 17 || b == 18)
						cursor += 4;
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		if (!files.isEmpty())
			throw new IllegalStateException("Unused " + files);
	}

}
