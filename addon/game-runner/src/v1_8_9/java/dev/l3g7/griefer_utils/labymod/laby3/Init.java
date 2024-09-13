/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3;

import dev.l3g7.griefer_utils.core.auto_update.AutoUpdater;
import dev.l3g7.griefer_utils.post_processor.EarlyPostProcessor;
import net.labymod.addon.AddonLoader;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipOutputStream;

import static dev.l3g7.griefer_utils.core.auto_update.AutoUpdater.DELETION_MARKER;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

@SuppressWarnings("unchecked")
public class Init implements IClassTransformer, AutoUpdater.Init {

	public Init() throws ReflectiveOperationException {
		if (System.setProperty("griefer_utils_load_flag", "") != null)
			throw new Error("GrieferUtils wurde bereits geladen!");

		// Add Java17to8Transpiler before every other transformer
		Field field = LaunchClassLoader.class.getDeclaredField("transformers");
		field.setAccessible(true);
		List<IClassTransformer> transformers = (List<IClassTransformer>) field.get(Launch.classLoader);
		transformers.add(0, EarlyPostProcessor.INSTANCE);

		if (!AutoUpdater.update(this))
			new Entrypoint().start();
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		return basicClass;
	}

	@Override
	public void forceDeleteJar(File jar) throws IOException {
		// Try to delete file directly
		if (jar.delete())
			return;

		// Minecraft's ClassLoader can create file leaks so the jar is probably locked.

		// Overwrite with empty zip file
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ZipOutputStream out = new ZipOutputStream(bout);
			out.setComment(DELETION_MARKER);
			out.close();
			Files.write(jar.toPath(), bout.toByteArray());
		} catch (Throwable t) {
			// Overwrite failed, but it doesn't matter
			t.printStackTrace(System.err);
		}

		// Add old file to LabyMod's .delete
		Path deleteFilePath = AddonLoader.getDeleteQueueFile().toPath();
		String deleteLine = jar.getName() + System.lineSeparator();
		Files.write(deleteFilePath, deleteLine.getBytes(), CREATE, APPEND);
	}

	@Override
	public String getLabyVersion() {
		return "laby3";
	}

}
