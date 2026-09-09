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
import dev.l3g7.griefer_utils.core.api.util.Util;
import dev.l3g7.griefer_utils.core.api.util.io.IO;
import dev.l3g7.griefer_utils.core.auto_update.AutoUpdater;
import dev.l3g7.griefer_utils.core.injection.InjectorBase;
import dev.pymdk.mapper.Mapping;
import net.labymod.addon.AddonLoader;
import net.labymod.core.asm.LabyModCoreMod;
import net.labymod.core.asm.LabyModTransformer;
import net.labymod.core.asm.mappings.Minecraft18MappingImplementation;
import net.labymod.core.asm.mappings.UnobfuscatedImplementation;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.CoreModManager;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;

@SuppressWarnings({"CharsetObjectCanBeUsed", "OptionalGetWithoutIsPresent"}) // Must be compatible with Java 8
public class Entrypoint implements AutoUpdater.Entrypoint {

	public void start() {
		// Load and inject libraries

		// PyMDK Mapper: For mapping reflection calls to Minecraft
		LibLoader.loadLibrary(
			"https://maven.pymdk.dev",
			"dev/pymdk", "mapper", "2.0.0",
			"8Spu5hcHpNijmfCyVDEdUjkXTn30g5izJG2RnXBQbk8="
		);

		// mXparser: for evaluating expressions (Calculator)
		LibLoader.loadLibrary(
			"https://repo1.maven.org/maven2",
			"org/mariuszgromada/math", "MathParser.org-mXparser", "6.1.0",
			"3OKEK/Y/TeoVTK5OdeBEC8iEsuYPRXdyahAZsmHlYn0="
		);

		// ZXing: for reading qr codes (QRCodeScanner)
		LibLoader.loadLibrary(
			"https://repo1.maven.org/maven2",
			"com/google/zxing", "core", "3.5.4",
			"cd5diTQbX89d2J2n9E6E2CXQ4ITN8+x3yaviaw8M6xM="
		);

		// Mixin: for modifying other classes (core.injection)
		LibLoader.loadLibrary(
			"https://repo.spongepowered.org/repository/maven-public",
			"org/spongepowered", "mixin", "0.7.11-SNAPSHOT", "20180703.121122-1",
			"2j1uR7nBK1oxLYm2e8J+JCnYI8Cc3oqQKZ6f3MTu/CA="
		);

		// Brigadier: for parsing commands (CommandSuggestions)
		LibLoader.loadLibrary(
			"https://libraries.minecraft.net",
			"com/mojang", "brigadier", "1.0.18",
			"7cSSaqS0kBD256xG79Yj+zj5UXNE0m9iUdeaJqlzjAs="
		);

		// Load mappings for automatic name resolution in Reflection
		Mapper.loadMappings(Launch.assetsDir.toPath(), LabyModCoreMod.isObfuscated() ? Mapping.OBFUSCATED : Mapping.INTERMEDIARY, true);

		Bridge.Initializer.init(LABY_3);

		// Sets LabyMod's mapping adapter
		// It's usually set in the MinecraftVisitor, but since Mixin changes the transformer order (i think),
		// transformers from LabyMod addons may be loaded before the MinecraftVisitor, causing the adapter to be null
		// and causing a crash if any addon tries to map something.
		Reflection.set(LabyModTransformer.class, "mappingImplementation", LabyModCoreMod.isObfuscated() ? new Minecraft18MappingImplementation() : new UnobfuscatedImplementation());

		// Cache classes with overwritten versions so Forge can read them
		// Forge's remapper loads the classes using getClassBytes, and puts them in a ClassReader, so a version of the
		// classes with a modified major version have to be loaded and cached manually to prevent crashes
		Map<String, byte[]> resourceCache = Reflection.get(Launch.classLoader, "resourceCache");

		for (String file : FileProvider.getFiles(f -> f.endsWith(".class"))) {
			byte[] bytes = IO.read(FileProvider.getData(file)).asBytes();
			bytes[7 /* major_version */] = 52 /* Java 1.8 */;
			resourceCache.put(file.substring(0, file.length() - 6), bytes);
		}

		// Load injector
		InjectorBase.inject();

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
