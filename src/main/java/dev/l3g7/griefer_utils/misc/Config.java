/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.util.IOUtil;
import net.minecraft.client.Minecraft;

import java.io.File;

import static dev.l3g7.griefer_utils.util.ArrayUtil.last;

/**
 * A class handling access and storage of the configuration.
 */
public class Config {

	/**
	 * Returns whether the given path exists.
	 */
    public static boolean has(String path) {
        if (path == null)
            return false;

        String[] parts = path.split("\\.");
        return getPath(parts).has(last(parts));
    }

	/**
	 * Returns the element stored at the given path, or null if no element is present.
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
	 * Returns the parent object of the given path.
	 */
    private static JsonObject getPath(String[] parts) {
        JsonObject o = getConfig();
        for (int i = 0; i < parts.length - 1; i++) {
            if (!o.has(parts[i]) || !(o.get(parts[i]).isJsonObject()))
                o.add(parts[i], new JsonObject());
            o = o.get(parts[i]).getAsJsonObject();
        }
        return o;
    }

	// .minecraft/config/GrieferUtils.json
    private static final File configFile = new File(new File(Minecraft.getMinecraft().mcDataDir, "config"), "GrieferUtils.json");
    private static JsonObject config = null;

	/**
	 * Writes the configuration to the config file.
	 */
    public static void save() {
	    if (config == null)
		    config = new JsonObject();

	    IOUtil.file(configFile).writeJson(config);
    }

	/**
	 * Reads the config file if necessary and returns the config object.
	 */
	private static JsonObject getConfig() {
		if (config == null)
			IOUtil.file(configFile)
				.readJsonObject(v -> config = v)
				.orElse(t -> config = new JsonObject());

		return config;
    }

}