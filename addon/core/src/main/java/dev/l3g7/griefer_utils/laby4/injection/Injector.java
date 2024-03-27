/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4.injection;

import dev.l3g7.griefer_utils.api.misc.functions.Runnable;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.auto_update.AutoUpdater;
import dev.l3g7.griefer_utils.injection.InjectorBase;
import net.labymod.api.Laby;
import net.labymod.api.addon.LoadedAddon;
import net.labymod.api.addon.entrypoint.Entrypoint;
import net.labymod.api.addon.transform.AddonClassTransformer;
import net.labymod.api.models.addon.annotation.AddonEntryPoint;
import net.labymod.api.models.addon.annotation.AddonTransformer;
import net.labymod.api.models.version.Version;
import net.minecraft.launchwrapper.Launch;
import sun.misc.Unsafe;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static java.nio.charset.StandardCharsets.UTF_8;

@AddonEntryPoint
@AddonTransformer
@SuppressWarnings("UnstableApiUsage")
public class Injector extends InjectorBase implements Entrypoint, AddonClassTransformer {

	@Override
	public void initialize(Version version) {
		try {
			// Determine operating system
			String os = System.getProperty("os.name").toLowerCase();
			String filename;
			if (os.contains("win")) {
				filename = "windows.exe";
			} else if (os.contains("mac")) {
				filename = "macos";
			} else if (os.contains("linux")) {
				filename = "linux";
			} else {
				throw new IllegalStateException("Could not determine operating system from '" + os + "'");
			}

			// Extract jattach executable
			Path executable = Files.createTempFile("griefer_utils_jattach", filename);
			Files.copy(Injector.class.getClassLoader().getResourceAsStream("jattach/" + filename), executable, StandardCopyOption.REPLACE_EXISTING);

			// Run executable
			String[] params = new String[]{executable.toString(), ManagementFactory.getRuntimeMXBean().getName().split("@")[0], "load", "instrument", "false", new File(getOwnJar()).toString()};
			Process res = Runtime.getRuntime().exec(params);

			// Capture output
			ByteArrayOutputStream err = new ByteArrayOutputStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			new Thread((Runnable) () -> res.getErrorStream().transferTo(err)).start();
			new Thread((Runnable) () -> res.getInputStream().transferTo(out)).start();
			int result = res.waitFor();

			Files.deleteIfExists(executable);

			if (System.setProperty("griefer_utils_agent_flag", "") == null) {
				System.err.println("Tried to execute agent using command: " + Arrays.toString(params));
				System.err.println("Agent injection returned exit code: " + result);
				System.out.write(out.toByteArray());
				System.err.write(err.toByteArray());
				throw new IllegalStateException("Agent injection failed.");
			}

			// Create elevated lookup
			MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
			Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			Unsafe unsafe = (Unsafe) theUnsafe.get(null);
			unsafe.putInt(LOOKUP, 12 /* allowedModes */, -1 /* TRUSTED */);

			// Enable mixing into LabyMod's classes
			HashSet<String> transformerExceptions = Reflection.get(Launch.classLoader, "transformerExceptions");
			var handle = LOOKUP.findVarHandle(HashSet.class, "map", HashMap.class);
			HashMap<?, ?> map = (HashMap<?, ?>) handle.get(transformerExceptions);

			var toRemove = transformerExceptions.stream()
				.filter(s -> s.startsWith("net.labymod")).toList();

			toRemove.forEach(map::remove);
		} catch (NoSuchFieldException | IllegalAccessException | IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}

		// Load InjectorBase
		LoadedAddon addon = Laby.labyAPI().addonService().getAddon(getClass()).orElseThrow();
		InjectorBase.initialize(addon.info().getNamespace());
	}

	private static String getOwnJar() {
		String ownJarUrl = Injector.class.getProtectionDomain().getCodeSource().getLocation().getFile();

		if (ownJarUrl.contains("!"))
			ownJarUrl = ownJarUrl.substring(0, ownJarUrl.lastIndexOf("!")); // remove class

		ownJarUrl = URLDecoder.decode(ownJarUrl, UTF_8);

		if (ownJarUrl.startsWith("file:/"))
			ownJarUrl = ownJarUrl.substring(5);

		return ownJarUrl;
	}

}
