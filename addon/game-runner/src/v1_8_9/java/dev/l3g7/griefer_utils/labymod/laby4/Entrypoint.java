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
import net.labymod.api.Laby;

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
		LibLoader.loadLibraries(

			// mXparser: for evaluating expressions (Calculator)
			"https://repo1.maven.org/maven2",
			"org/mariuszgromada/math", "MathParser.org-mXparser", "6.1.0",
			"DCE2842BF63F4DEA154CAE4E75E0440BC884B2E60F4577726A1019B261E5627D",

			// ZXing: for reading qr codes (QRCodeScanner)
			"https://repo1.maven.org/maven2",
			"com/google/zxing", "core", "3.5.4",
			"71DE5D89341B5FCF5DD89DA7F44E84D825D0E084CDF3EC77C9ABE26B0F0CEB13",

			// Brigadier: for parsing commands (CommandSuggestions)
			"https://libraries.minecraft.net",
			"com/mojang", "brigadier", "1.0.18",
			"EDC4926AA4B49010F6E7AC46EFD623FB38F9517344D26F6251D79A26A9738C0B"
		);
	}

}
