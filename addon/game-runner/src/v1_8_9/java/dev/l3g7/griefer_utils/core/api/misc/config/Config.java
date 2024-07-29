/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static dev.l3g7.griefer_utils.core.api.util.ArrayUtil.last;

/**
 * A class handling access and storage of the configuration.
 */
public class Config {

	/**
	 * @return whether the given path exists.
	 */
    public static boolean has(String path) {
        if (path == null)
            return false;

        String[] parts = path.split("\\.");
        return getPath(parts).has(last(parts));
    }

	/**
	 * @return the element stored at the given path, or null if no element is present.
	 */
    public static JsonElement get(String path) {
        String[] parts = path.split("\\.");
        return getPath(parts).get(last(parts));
    }

	/**
	 * Stores the given json element at the given path.
	 */
    public static void set(String path, JsonElement val) {
        String[] parts = path.split("\\.");
        getPath(parts).add(last(parts), val);
    }

	/**
	 * @return the parent object of the given path.
	 */
    private static JsonObject getPath(String[] parts) {
        JsonObject o = get();
        for (int i = 0; i < parts.length - 1; i++) {
            if (!o.has(parts[i]) || !(o.get(parts[i]).isJsonObject()))
                o.add(parts[i], new JsonObject());
            o = o.get(parts[i]).getAsJsonObject();
        }
        return o;
    }

	private static final Object SAVE_LOCK = new Object();
	// .minecraft/config/GrieferUtils.json
    private static final File configFile = new File(new File("config"), "GrieferUtils.json");
	private static final File newConfigFile = new File(new File("config"), "GrieferUtils-new.json");
	private static int hash = 0;
    private static JsonObject config = null;

	/**
	 * Writes the configuration to the config file.
	 */
    public static void save() {
	    if (config == null)
		    config = new JsonObject();

	    String json = IOUtil.gson.toJson(config);

	    synchronized (SAVE_LOCK) {
		    if (json.hashCode() == hash) // Check if content has changed
			    return;

		    hash = json.hashCode();
		    save(json, newConfigFile);
		    save(json, configFile);
	    }
	    try {
		    Files.deleteIfExists(newConfigFile.toPath());
	    } catch (IOException ignored) {}
    }

	/**
	 * Writes the configuration to the given file.
	 */
	public static void save(String json, File file) {
		do {
			IOUtil.write(file, json);
		} while (IOUtil.gson.toJson(read(file)).hashCode() != hash);
	}

	/**
	 * Lazy loads the config if required and returns it.
	 */
	public static JsonObject get() {
		if (config == null) {
			if (!configFile.exists()) {
				config = new JsonObject();
				return config;
			}

			if (!newConfigFile.exists() || !loadFile(newConfigFile))
				loadFile(configFile);

			new ConfigPatcher(config).patch();
		}

		return config;
    }

	/**
	 * Tries to load the config from the given file, returning whether it was successful.
	 */
	private static boolean loadFile(File file) {
		try {
			config = IOUtil.read(file).asJsonObject().orElseThrow(Throwable::new);
			return config.entrySet().size() != 0;
		} catch (Throwable t) {
			return false;
		}
	}

	/**
	 * Reads the config from its file, returning an empty one if it fails.
	 */
	private static JsonObject read(File file) {
		return IOUtil.read(file)
			.asJsonObject()
			.orElse(new JsonObject());
	}

}