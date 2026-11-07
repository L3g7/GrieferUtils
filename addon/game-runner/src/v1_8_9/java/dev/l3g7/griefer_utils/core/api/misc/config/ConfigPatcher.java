/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.misc.config.patches.*;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map.Entry;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.api.misc.VersionComparator.VERSION_COMPARATOR;
import static dev.l3g7.griefer_utils.core.api.reflection.Reflection.c;

/**
 * Migrates old configs.
 */
public class ConfigPatcher {

	public static boolean versionChanged = false;

	public static void patch(JsonObject config) {
		if (!config.has("version")) {
			config.addProperty("version", labyBridge.addonVersion());
			return;
		}

		String configVersion = config.get("version").getAsString();
		String newVersion = labyBridge.addonVersion();
		if (newVersion.equals(configVersion))
			return;

		ConfigBackuper.backup(configVersion);
		config.addProperty("version", newVersion);
		versionChanged = true;

		ConfigPatch[] patches = new ConfigPatch[]{
			new To2_0_BETA_13_2(),
			new To2_0_BETA_14(),
			new To2_0_RC_8(),
			new To2_0_RC_9(),
			new To2_0_RC_12(),
			new To2_0(),
			new To2_2_BETA_1(),
			new To2_2_BETA_6(),
			new To2_3_BETA_3(),
			new To2_3_BETA_6(),
			new To2_3_BETA_8(),
			new To2_3_BETA_14(),
			new To2_3_BETA_15(),
			new To2_3_BETA_18(),
			new To2_3_BETA_24(),
			new To2_3(),
			new To2_4_BETA_1(),
			new To2_4_BETA_5(),
			new To2_4_8_BETA_1(),
			new To2_4_8_BETA_8(),
			new Cleanup()
		};

		for (var patch : patches) {
			patch.init(config, configVersion);
			if (patch.shouldApply())
				patch.patch();
		}
	}

	public abstract static class ConfigPatch {

		protected JsonObject config;
		protected String configVersion;

		// Don't use constructor as it propagates
		private void init(JsonObject config, String configVersion) {
			this.config = config;
			this.configVersion = configVersion;
		}

		protected abstract boolean shouldApply();

		protected abstract void patch();

		protected boolean isConfigOlderThan(@Pattern("^v?(?<version>(?:\\d+\\.)*\\d+)(?:-(?<meta>[\\w.+-]+?)(?<metaid>[\\d.]*))?$") String version) {
			return VERSION_COMPARATOR.compare(version, configVersion) < 0;
		}

		protected void rename(String oldPath, String newPath) {
			if (getParentOf(oldPath).has(keyOf(oldPath))) {
				var value = getParentOf(oldPath).remove(keyOf(oldPath));
				getParentOf(newPath).add(keyOf(newPath), value);
			}
		}

		protected void rename(String parent, String oldKey, String newKey) {
			rename(parent + "." + oldKey, parent + "." + newKey);
		}

		protected @NotNull ConfigObject get(String path) {
			return new ConfigObject(get0(path));
		}

		private JsonObject getParentOf(String child) {
			int idx = child.lastIndexOf('.');
			if (idx == -1)
				return config;

			return get0(child.substring(0, idx));
		}

		private static String keyOf(String path) {
			int idx = path.lastIndexOf('.');
			if (idx == -1)
				return path;

			return path.substring(idx + 1);
		}

		private JsonObject get0(String path) {
			String[] parts = path.split("\\.");
			JsonObject obj = config;
			for (String part : parts) {
				if (!obj.has(part) || !(obj.get(part).isJsonObject()))
					obj.add(part, new JsonObject());
				obj = obj.getAsJsonObject(part);
			}
			return obj;
		}

	}

	/**
	 * JsonObject helper
	 */
	public static class ConfigObject implements Iterable<Entry<String, JsonElement>> {
		private final JsonObject wrapped;

		private ConfigObject(JsonObject wrapped) {this.wrapped = wrapped;}

		public JsonObject unwrap() {
			return wrapped;
		}

		public @NotNull ConfigPatcher.ConfigObject getObject(String memberName) {
			if (!has(memberName))
				set(memberName, new JsonObject());

			return new ConfigObject(wrapped.getAsJsonObject(memberName));
		}

		public Iterable<JsonObject> getObjectArray(String memberName) {
			return new Iterable<>() {
				public @NotNull Iterator<JsonObject> iterator() {
					return c(wrapped.getAsJsonArray(memberName).iterator());
				}
			};
		}

		public boolean isTrue(String memberName) {
			var element = wrapped.get(memberName);
			return element != null && element.getAsBoolean();
		}

		@Override
		public @NotNull Iterator<Entry<String, JsonElement>> iterator() {
			return wrapped.entrySet().iterator();
		}

		// Indirect proxies
		public JsonArray getJsonArray(String memberName) {return wrapped.getAsJsonArray(memberName);}

		public void set(String property, JsonElement value) {
			wrapped.add(property, value);
		}

		public void set(String property, String value) {
			wrapped.addProperty(property, value);
		}

		public void set(String property, Boolean value) {
			wrapped.addProperty(property, value);
		}

		// Direct proxies
		public boolean has(String memberName) {return wrapped.has(memberName);}

		public JsonElement get(String memberName) {return wrapped.get(memberName);}

		public void remove(String property) {wrapped.remove(property);}
	}
}
