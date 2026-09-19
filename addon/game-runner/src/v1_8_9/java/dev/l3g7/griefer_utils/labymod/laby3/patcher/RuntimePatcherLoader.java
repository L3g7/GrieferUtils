/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.patcher;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.List;
import java.util.UUID;

/**
 * Overwrites the class version of all classes to fix OW2 ASM parsing and registers the {@link RuntimePatcher}
 * after the renameTransformer has been loaded.
 */
public class RuntimePatcherLoader implements IClassTransformer {

	public static final RuntimePatcherLoader INSTANCE = new RuntimePatcherLoader();

	private static final Field modCountField;
	private static final Field renameTransformerField;

	/**
	 * True if RuntimePatcher has been added as its own transformer.
	 */
	private boolean decoupled = false;

	static {
		try {
			modCountField = AbstractList.class.getDeclaredField("modCount");
			modCountField.setAccessible(true);
			renameTransformerField = LaunchClassLoader.class.getDeclaredField("renameTransformer");
			renameTransformerField.setAccessible(true);
			RuntimePatcher.preload(); // Load RuntimePatcher class beforehand so we don't transform it
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] classBytes) {
		String fileName = transformedName.replace('.', '/').concat(".class");
		if (!fileName.startsWith("dev/l3g7/griefer_utils/"))
			return classBytes;

		classBytes[7 /* major_version */] = 52 /* Java 1.8 */;

		if (decoupled || checkRegisterPatcher()) {
			// Patcher was added to transformers, don't invoke manually
			return classBytes;
		}

		return RuntimePatcher.INSTANCE.transform(fileName, transformedName, classBytes);
	}

	private boolean checkRegisterPatcher() {
		try {
			if (renameTransformerField.get(Launch.classLoader) != null) {
				// renameTransformer is available, add patchers to end of transformers
				decoupled = true;
				addRuntimePatcher();
				return true;
			}
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		return false;
	}

	private void addRuntimePatcher() throws IllegalAccessException, NoSuchFieldException {
		Field transformersField = LaunchClassLoader.class.getDeclaredField("transformers");
		transformersField.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<IClassTransformer> transformers = (List<IClassTransformer>) transformersField.get(Launch.classLoader);

		// Add patcher
		int modCount = (int) modCountField.get(transformers);
		transformers.add(RuntimePatcher.INSTANCE);

		// Fake modCount to avoid a ConcurrentModificationException
		modCountField.set(transformers, modCount);

		// Force rebuilding of MixinServiceLaunchWrapper's transformers
		//noinspection deprecation // Not deprecated in LabyMod 3
		MixinEnvironment.getCurrentEnvironment().addTransformerExclusion(UUID.randomUUID().toString());
	}

}
