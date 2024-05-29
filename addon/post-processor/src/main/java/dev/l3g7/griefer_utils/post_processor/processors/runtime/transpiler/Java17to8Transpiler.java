/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.runtime.transpiler;

import dev.l3g7.griefer_utils.post_processor.processors.RuntimePostProcessor;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.objectweb.asm.ClassWriter;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Invokes or adds all transpiler parts.
 */
public class Java17to8Transpiler extends RuntimePostProcessor {

	public static final Java17to8Transpiler INSTANCE = new Java17to8Transpiler();

	private static final List<RuntimePostProcessor> earlyTranspilers = Collections.singletonList(new ClassVersionPatcher());
	// Transpilers that require renamed classes i.e. that must be triggered after the renameTransformer
	private static final List<RuntimePostProcessor> lateTranspilers = Arrays.asList(
		new StringConcatPolyfill(),
		new AccessElevator()
		new SuperclassRemapper()
	);

	private static final Field modCountField;
	private static final Field renameTransformerField;
	private static boolean decoupled = false;

	static {
		try {
			modCountField = AbstractList.class.getDeclaredField("modCount");
			modCountField.setAccessible(true);
			renameTransformerField = LaunchClassLoader.class.getDeclaredField("renameTransformer");
			renameTransformerField.setAccessible(true);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] transform(String fileName, byte[] classBytes) {
		if (!fileName.startsWith("dev/l3g7/griefer_utils/"))
			return classBytes;

		for (RuntimePostProcessor transpiler : earlyTranspilers)
			classBytes = transpiler.transform(fileName, classBytes);

		if (decoupled || addLateTranspilers()) {
			// Late transpilers were added to transformers, don't invoke manually
			return classBytes;
		}

		for (RuntimePostProcessor transpiler : lateTranspilers)
			classBytes = transpiler.transform(fileName, classBytes);

		return classBytes;
	}

	private boolean addLateTranspilers() {
		try {
			if (renameTransformerField.get(Launch.classLoader) != null) {
				// renameTransformer is available, add late transpiler to end of transformers
				decoupled = true;

				Field transformersField = LaunchClassLoader.class.getDeclaredField("transformers");
				transformersField.setAccessible(true);
				@SuppressWarnings("unchecked")
				List<IClassTransformer> transformers = (List<IClassTransformer>) transformersField.get(Launch.classLoader);

				// Add transpilers
				int modCount = (int) modCountField.get(transformers);
				transformers.addAll(lateTranspilers);

				// Fake modCount to avoid a ConcurrentModificationException
				modCountField.set(transformers, modCount);
				return true;
			}
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		return false;
	}

	/**
	 * A ClassWriter loaded by the same ClassLoader as the transpiler. As {@link ClassWriter} is normally
	 * loaded by the parent ClassLoader of the one loading this addon, it wouldn't find the classes
	 * defined by its child ClassLoader and getCommonSuperClass calls would fail.
	 */
	static class BoundClassWriter extends ClassWriter {
		public BoundClassWriter() {
			super(COMPUTE_MAXS | COMPUTE_FRAMES);
		}
	}

}
