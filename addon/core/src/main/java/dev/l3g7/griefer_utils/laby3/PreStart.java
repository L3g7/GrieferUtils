/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby3;

import dev.l3g7.griefer_utils.post_processor.processors.runtime.Java17to8Transpiler;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

@SuppressWarnings({"unused", "unchecked"})
public class PreStart implements IClassTransformer {

	public PreStart() throws IOException, ReflectiveOperationException {
		if (System.setProperty("griefer_utils_load_flag", "") != null)
			throw new Error("GrieferUtils wurde bereits geladen!");

		// Add Java17to8Transpiler before every other transformer
		Field field = LaunchClassLoader.class.getDeclaredField("transformers");
		field.setAccessible(true);
		List<IClassTransformer> transformers = (List<IClassTransformer>) field.get(Launch.classLoader);
		transformers.add(0, Java17to8Transpiler.INSTANCE);

		// TODO AutoUpdater.update()
		EarlyStart.start();
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		return basicClass;
	}

}
