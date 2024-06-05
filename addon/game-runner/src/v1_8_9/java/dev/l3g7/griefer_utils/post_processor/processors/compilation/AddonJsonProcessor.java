/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.post_processor.processors.compilation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import dev.l3g7.griefer_utils.post_processor.processors.CompilationPostProcessor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Adds all missing LabyMod 3 properties to /addon.json.
 */
public class AddonJsonProcessor extends CompilationPostProcessor {

	public static final AddonJsonProcessor INSTANCE = new AddonJsonProcessor();

	private AddonJsonProcessor() {}

	@Override
	public void apply(FileSystem fs) throws IOException {
		Path addonJson = fs.getPath("/addon.json");

		JsonObject addon;
		try (Reader in = new InputStreamReader(Files.newInputStream(addonJson), UTF_8)) {
			addon = Streams.parse(new JsonReader(in)).getAsJsonObject();
		}

		// Write addon.json
		try (OutputStream out = Files.newOutputStream(addonJson)) {
			addon.addProperty("uuid", "%uuid%");
			addon.addProperty("name", "GrieferUtils");
			addon.addProperty("icon", "griefer_utils_icon");
			addon.addProperty("debug", System.getProperty("griefer_utils.debug"));
			addon.addProperty("transformerClass", "dev.l3g7.griefer_utils.labymod.laby3.PreStart");
			addon.addProperty("addonVersion", addon.get("version").getAsString());

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			out.write(gson.toJson(addon).getBytes(StandardCharsets.ISO_8859_1));
		}
	}

}
