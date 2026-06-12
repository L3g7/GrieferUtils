/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4;

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.mapping.Mapper;
import dev.l3g7.griefer_utils.core.api.misc.LibLoader;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.auto_update.AutoUpdater;
import dev.l3g7.griefer_utils.labymod.laby4.injection.Injector;
import net.labymod.api.Laby;
import net.minecraft.launchwrapper.Launch;

import java.io.InputStreamReader;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

public class Entrypoint implements AutoUpdater.Entrypoint {

	public void start() {
		Bridge.Initializer.init(LABY_4);

		// Ensure addon version is up-to-date
		JsonObject addonJson = Streams.parse(new JsonReader(new InputStreamReader(FileProvider.getData("addon.json")))).getAsJsonObject();
		Reflection.set(Laby.labyAPI().addonService().getAddon(Main.class).orElseThrow().info(), "version", addonJson.get("version").getAsString());

		// Load mcp mappings for automatic name resolution in Reflection
		Mapper.loadMappings("1.8.9", "22");

		// Load and inject libraries

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
		// Brigadier: for parsing commands (CommandSuggestions)
		LibLoader.loadLibrary(
			"https://libraries.minecraft.net",
			"com/mojang", "brigadier", "1.0.18",
			"EDC4926AA4B49010F6E7AC46EFD623FB38F9517344D26F6251D79A26A9738C0B"
		);

		// Load injector
		Launch.classLoader.registerTransformer(Injector.class.getName());
	}

}
