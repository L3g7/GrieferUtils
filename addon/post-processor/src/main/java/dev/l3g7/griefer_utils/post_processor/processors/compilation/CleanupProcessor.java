/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.compilation;

import dev.l3g7.griefer_utils.post_processor.processors.CompilationPostProcessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.jar.Manifest;
import java.util.stream.Stream;

/**
 * Removes all unused files and adds a manifest version.
 */
public class CleanupProcessor extends CompilationPostProcessor {

	public static final CleanupProcessor INSTANCE = new CleanupProcessor();

	private CleanupProcessor() {}

	@Override
	public void apply(FileSystem fs) throws IOException {
		// delete LabyMod 4 autogen
		deleteDir(fs.getPath("dev/l3g7/griefer_utils/core"));

		// delete compilation post processors
		deleteDir(fs.getPath("dev/l3g7/griefer_utils/post_processor/processors/compilation"));
		Files.deleteIfExists(fs.getPath("dev/l3g7/griefer_utils/post_processor/processors/CompilationPostProcessor.class"));
		Files.deleteIfExists(fs.getPath("dev/l3g7/griefer_utils/post_processor/PostProcessor.class"));

		deleteDir(fs.getPath("META-INF/custom-services"));

		// Fix manifest version
		Path manifestPath = fs.getPath("META-INF/MANIFEST.MF");
		Manifest manifest = new Manifest(Files.newInputStream(manifestPath));
		if (!manifest.getMainAttributes().containsKey("Manifest-Version")) {
			// LabyMod's manifest generator doesn't include an empty newline at the end of the file.
			// This results in the Manifest-Version getting skipped if re-generated (e.g. by jarsigner).
			manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		manifest.write(out);
		Files.write(manifestPath, out.toByteArray());
	}

	private void deleteDir(Path obj) throws IOException {
		if (!Files.exists(obj))
			return;

		try (Stream<Path> walk = Files.walk(obj)) {
			walk.sorted(Comparator.reverseOrder())
				.forEach(path -> {
					try {
						Files.delete(path);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
		}
	}

}
