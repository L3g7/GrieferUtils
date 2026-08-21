/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.misc.DebounceTimer;
import dev.l3g7.griefer_utils.core.api.util.io.IO;
import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.l3g7.griefer_utils.core.api.util.ArrayUtil.last;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.*;

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
		JsonObject obj = getPath(parts, false);
		return obj != null && obj.has(last(parts));
	}

	/**
	 * @return the element stored at the given path, or null if no element is present.
	 */
	public static JsonElement get(String path) {
		String[] parts = path.split("\\.");
		return getPath(parts, true).get(last(parts));
	}

	/**
	 * Stores the given json element at the given path.
	 */
	public static void set(String path, JsonElement val) {
		String[] parts = path.split("\\.");
		getPath(parts, true).add(last(parts), val);
	}

	/**
	 * Removes the json element at the given path.
	 */
	public static void unset(String path) {
		String[] parts = path.split("\\.");
		JsonObject obj = getPath(parts, false);
		if (obj != null) {
			obj.remove(last(parts));
			if (parts.length > 1 && obj.entrySet().isEmpty())
				unset(path.substring(0, path.length() - last(parts).length() - 1));
		}
	}

	/**
	 * @return the parent object of the given path.
	 */
	@Contract("_, false -> _; _, true -> !null")
	private static JsonObject getPath(String[] parts, boolean initialize) {
		JsonObject o = get();
		for (int i = 0; i < parts.length - 1; i++) {
			if (!o.has(parts[i]) || !(o.get(parts[i]).isJsonObject())) {
				if (initialize)
					o.add(parts[i], new JsonObject());
				else
					return null;
			}
			o = o.get(parts[i]).getAsJsonObject();
		}
		return o;
	}

	private static final Object SAVE_LOCK = new Object();
	private static final DebounceTimer debounceTimer = new DebounceTimer("Config", 1000);
	// .minecraft/config/GrieferUtils.json
	protected static final Path configFile = Path.of("config", "GrieferUtils.json");
	private static final Path newConfigFile = Path.of("config", "GrieferUtils-new.json");
	private static int hash = 0;
	private static JsonObject config = null;

	/**
	 * Writes the configuration to the config file using an atomic move.
	 */
	public static void save() {
		debounceTimer.schedule(() -> {
			if (config == null)
				config = new JsonObject();

			String json = IO.GSON_PRETTY.toJson(config);

			synchronized (SAVE_LOCK) {
				// Check if content has changed
				if (json.hashCode() == hash)
					return;

				// Write to newConfigFile
				hash = json.hashCode();
				do {
					IO.write(newConfigFile).value(json);
				} while (IO.read(newConfigFile).asString().hashCode() != hash);

				// Move newConfigFile to configFile
				try {
					try {
						Files.move(newConfigFile, configFile, REPLACE_EXISTING, ATOMIC_MOVE);
					} catch (AtomicMoveNotSupportedException e) {
						Files.move(newConfigFile, configFile, REPLACE_EXISTING);
					}
				} catch (AccessDeniedException e) {
					// TODO: cleanup
					//noinspection ReadWriteStringCanBeUsed
					Files.write(configFile, json.getBytes(StandardCharsets.UTF_8), WRITE, TRUNCATE_EXISTING, CREATE);
					try {
						Files.deleteIfExists(newConfigFile);
					} catch (IOException ignored) {}
				}
			}
		});
	}

	/**
	 * Lazy loads the config if required and returns it.
	 */
	public static JsonObject get() {
		if (config == null) {
			if (Files.notExists(configFile)) {
				config = new JsonObject();
				new ConfigPatcher(config).patch();
				return config;
			}

			if (Files.notExists(newConfigFile) || !loadPath(newConfigFile)) {
				if (!loadPath(configFile)) {
					// Config failed to load
					ConfigBackuper.backup("error");
					config = new JsonObject();
				}
			}

			new ConfigPatcher(config).patch();
		}

		return config;
	}

	/**
	 * Tries to load the config from the given path, returning whether it was successful.
	 */
	private static boolean loadPath(Path file) {
		return IO.read(file).tryAsJsonObject().map(data -> {
			config = data;
			return !config.entrySet().isEmpty();
		}).unwrapOr(false);
	}

}