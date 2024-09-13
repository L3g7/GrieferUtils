/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.mapping.Mapper;
import dev.l3g7.griefer_utils.core.api.misc.LibLoader;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.auto_update.AutoUpdater;
import dev.l3g7.griefer_utils.labymod.laby3.injection.Injector;
import net.labymod.addon.AddonLoader;
import net.labymod.core.asm.LabyModCoreMod;
import net.labymod.core.asm.LabyModTransformer;
import net.labymod.core.asm.mappings.Minecraft18MappingImplementation;
import net.labymod.core.asm.mappings.UnobfuscatedImplementation;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.CoreModManager;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;

@SuppressWarnings({"CharsetObjectCanBeUsed", "OptionalGetWithoutIsPresent"}) // Must be compatible with Java 8
public class Entrypoint implements AutoUpdater.Entrypoint {

	public void start() {
		Bridge.Initializer.init(LABY_3);

		// Load mcp mappings for automatic name resolution in Reflection
		Mapper.loadMappings("1.8.9", "22");

		// Load and inject libraries
		LibLoader.loadLibraries(
			// mXparser: for evaluating expressions (Calculator)
			"https://repo1.maven.org/maven2",
			"org/mariuszgromada/math", "MathParser.org-mXparser", "5.1.0",
			"B5472B5E1BBEFEA2DA6052C68A509C84C7F2CA5F99B76A4C5F58354C08818630",

			// ZXing: for reading qr codes (QRCodeScanner)
			"https://repo1.maven.org/maven2",
			"com/google/zxing", "core", "3.5.1",
			"1BA7C0FBB6C267E2FB74E1497D855ADAE633CCC98EDC8C75163AA64BC08E3059",

			// Mixin: for modifying other classes (core.injection)
			"https://repo.spongepowered.org/repository/maven-public",
			"org/spongepowered", "mixin", "0.7.11-SNAPSHOT", "mixin-0.7.11-20180703.121122-1.jar",
			"DA3D6E47B9C12B5A312D89B67BC27E2429D823C09CDE8A90299E9FDCC4EEFC20"
		);

		// Sets LabyMod's mapping adapter
		// It's usually set in the MinecraftVisitor, but since Mixin changes the transformer order (i think),
		// transformers from LabyMod addons may be loaded before the MinecraftVisitor, causing the adapter to be null
		// and causing a crash if any addon tries to map something.
		Reflection.set(LabyModTransformer.class, "mappingImplementation", LabyModCoreMod.isObfuscated() ? new Minecraft18MappingImplementation() : new UnobfuscatedImplementation());

		// Cache classes with overwritten versions so Forge can read them
		// Forge's remapper loads the classes using getClassBytes, and puts them in a ClassReader, so a version of the
		// classes with a modified major version have to be loaded and cached manually to prevent crashes
		Map<String, byte[]> resourceCache = Reflection.get(Launch.classLoader, "resourceCache");

		try {
			for (String file : FileProvider.getFiles(f -> f.endsWith(".class"))) {
				byte[] bytes = IOUtil.toByteArray(FileProvider.getData(file));
				bytes[7 /* major_version */] = 52 /* Java 1.8 */;
				resourceCache.put(file.substring(0, file.length() - 6), bytes);
			}
		} catch (IOException e) {
			throw Util.elevate(e);
		}

		// Add Injector as transformer
		Launch.classLoader.registerTransformer(Injector.class.getName());

		if (labyBridge.forge()) {
			// Add own file to ignored mods so Forge doesn't try to read this jar
			String jarPath = Entrypoint.class.getProtectionDomain().getCodeSource().getLocation().getFile();
			if (!jarPath.contains(".jar"))
				return;

			try {
				jarPath = jarPath.substring(5, jarPath.lastIndexOf("!"));
				jarPath = URLDecoder.decode(jarPath, "UTF-8");
				CoreModManager.getIgnoredMods().add(new File(jarPath).getName());
			} catch (UnsupportedEncodingException e) {
				throw Util.elevate(e);
			}
		}

		// Fix main class
		Map<UUID, String> names = Reflection.get(AddonLoader.class, "names");
		UUID addonUuid = names.entrySet().stream().filter(e -> e.getValue().equals("GrieferUtils")).findFirst().get().getKey();

		Map<UUID, String> mainClasses = Reflection.get(AddonLoader.class, "mainClasses");
		mainClasses.put(addonUuid, Main.class.getName());

		// Fix addon version
		Map<UUID, JsonObject> loadedOffline = Reflection.get(AddonLoader.class, "loadedOffline");
		loadedOffline.get(addonUuid).getAsJsonObject().addProperty("version", 1);
	}

}
