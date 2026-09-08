/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.mapping.Mapper;
import dev.l3g7.griefer_utils.core.api.misc.LibLoader;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.api.util.io.IO;
import dev.l3g7.griefer_utils.core.auto_update.AutoUpdater;
import dev.l3g7.griefer_utils.core.injection.InjectorBase;
import dev.pymdk.mapper.Mapping;
import net.labymod.api.Laby;
import net.minecraft.launchwrapper.Launch;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_4;

public class Entrypoint implements AutoUpdater.Entrypoint {

	public void start() {
		Bridge.Initializer.init(LABY_4);

		// Load and inject libraries

		// PyMDK Mapper: For mapping reflection calls to Minecraft
		LibLoader.loadLibrary(
			"https://maven.pymdk.dev",
			"dev/pymdk", "mapper", "2.0.0",
			"9BuVn5MohzeAyh7Bc4VX8wmQE+G4L0k2sgWmSpl0BuM="
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
		// Brigadier: for parsing commands (CommandSuggestions)
		LibLoader.loadLibrary(
			"https://libraries.minecraft.net",
			"com/mojang", "brigadier", "1.0.18",
			"7cSSaqS0kBD256xG79Yj+zj5UXNE0m9iUdeaJqlzjAs="
		);

		// Load mappings for automatic name resolution in Reflection
		Mapper.loadMappings(Launch.assetsDir.toPath(), Mapping.INTERMEDIARY, false);

		// Ensure addon version is up-to-date
		JsonObject addonJson = IO.read(FileProvider.getData("addon.json")).asJsonObject();
		Reflection.set(Laby.labyAPI().addonService().getAddon(Main.class).orElseThrow().info(), "version", addonJson.get("version").getAsString());

		// Load injector
		InjectorBase.inject();
	}

}
