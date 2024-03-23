/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor;

import dev.l3g7.griefer_utils.post_processor.processors.CompilationPostProcessor;
import dev.l3g7.griefer_utils.post_processor.processors.compilation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class PostProcessor {

	private static final List<CompilationPostProcessor> COMPILATION_PROCESSORS = Arrays.asList(AddonJsonProcessor.INSTANCE, ClassVersionProcessor.INSTANCE, RefmapConverter.INSTANCE, CleanupProcessor.INSTANCE);

	public static void main(String[] args) throws IOException {
		String version = System.getProperty("griefer_utils.version");

		// Rename jar
		File jar = new File("..\\build\\libs\\GrieferUtils-release.jar");
		File newJar = new File(jar.getParentFile(), "griefer-utils-v" + version + ".jar");
		Files.copy(jar.toPath(), newJar.toPath(), REPLACE_EXISTING);

		// Apply compilation post processors
		try (FileSystem fs = FileSystems.newFileSystem(newJar.toPath())) {
			for (CompilationPostProcessor processor : COMPILATION_PROCESSORS)
				processor.apply(fs);
		}
	}

}
