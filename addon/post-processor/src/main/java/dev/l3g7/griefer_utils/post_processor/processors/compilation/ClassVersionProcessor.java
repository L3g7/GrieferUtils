/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.compilation;

import dev.l3g7.griefer_utils.post_processor.PostProcessor;
import dev.l3g7.griefer_utils.post_processor.processors.CompilationPostProcessor;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Overwrites the class version of the bootstrap classes to allow execution in Java 8.
 */
public class ClassVersionProcessor extends CompilationPostProcessor {

	public static final ClassVersionProcessor INSTANCE = new ClassVersionProcessor();

	private static final String POST_PROCESSOR_FOLDER = PostProcessor.class.getPackage().getName().replace('.', '/');

	private static final Predicate<Path> BOOTSTRAP_CLASSES =
		p -> p.startsWith("dev/l3g7/griefer_utils/laby3/PreStart.class")
			|| p.startsWith(POST_PROCESSOR_FOLDER + "/processors/runtime/transpiler/")
			|| p.startsWith(POST_PROCESSOR_FOLDER + "/processors/RuntimePostProcessor.class");

	private ClassVersionProcessor() {}

	@Override
	public void apply(FileSystem fs) throws IOException {
		// Overwrite class versions
		try (Stream<Path> stream = Files.walk(fs.getPath(""))) {
			stream
				.filter(p -> !Files.isDirectory(p))
				.filter(BOOTSTRAP_CLASSES)
				.forEach(path -> {
					try {
						byte[] bytes = Files.readAllBytes(path);
						bytes[7 /* major_version */] = 52 /* Java 1.8 */;
						Files.write(path, bytes);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
		}
	}

}
