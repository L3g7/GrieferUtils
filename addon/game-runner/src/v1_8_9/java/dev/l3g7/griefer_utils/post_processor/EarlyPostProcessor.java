/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.List;

/**
 * Overwrites the class version of all classes to fix OW2 ASM parsing and handles invocation and
 * registration of the processors.
 * @see LatePostProcessor
 */
public class EarlyPostProcessor implements IClassTransformer {

	public static final EarlyPostProcessor INSTANCE = new EarlyPostProcessor();

	private static final Field modCountField;
	private static final Field renameTransformerField;

	private final LatePostProcessor lateProcessor = new LatePostProcessor();
	private boolean decoupled = false;

	static {
		if (Launch.classLoader == null) {
			// Minecraft not started, transformers of LaunchClassLoader not available
			modCountField = null;
			renameTransformerField = null;
		} else {
			try {
				modCountField = AbstractList.class.getDeclaredField("modCount");
				modCountField.setAccessible(true);
				renameTransformerField = LaunchClassLoader.class.getDeclaredField("renameTransformer");
				renameTransformerField.setAccessible(true);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] classBytes) {
		String fileName = transformedName.replace('.', '/').concat(".class");
		if (!fileName.startsWith("dev/l3g7/griefer_utils/"))
			return classBytes;

		classBytes[7 /* major_version */] = 52 /* Java 1.8 */;

		if (decoupled || registerProcessors()) {
			// Processors were added to transformers, don't invoke manually
			return classBytes;
		}

		classBytes = lateProcessor.transform(fileName, transformedName, classBytes);
		return classBytes;
	}

	private boolean registerProcessors() {
		if (renameTransformerField == null)
			return false;

		try {
			if (renameTransformerField.get(Launch.classLoader) != null) {
				// renameTransformer is available, add processors to end of transformers
				decoupled = true;

				Field transformersField = LaunchClassLoader.class.getDeclaredField("transformers");
				transformersField.setAccessible(true);
				@SuppressWarnings("unchecked")
				List<IClassTransformer> transformers = (List<IClassTransformer>) transformersField.get(Launch.classLoader);

				// Add processors
				@SuppressWarnings("DataFlowIssue") // IntelliJ is stupid
				int modCount = (int) modCountField.get(transformers);
				transformers.add(lateProcessor);

				// Fake modCount to avoid a ConcurrentModificationException
				modCountField.set(transformers, modCount);
				return true;
			}
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		return false;
	}


}
