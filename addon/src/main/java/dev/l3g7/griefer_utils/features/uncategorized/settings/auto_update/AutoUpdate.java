/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.features.uncategorized.settings.auto_update;

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.AddonUtil;
import dev.l3g7.griefer_utils.core.util.IOUtil;
import dev.l3g7.griefer_utils.core.misc.Config;
import net.labymod.addon.AddonLoader;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class AutoUpdate {

	public static final BooleanSetting enabled = new BooleanSetting()
		.name("Automatisch updaten")
		.description("Updatet GrieferUtils automatisch auf die neuste Version.")
		.config("settings.auto_update.enabled")
		.icon("arrow_circle")
		.defaultValue(true);

	public static void checkForUpdate(UUID addonUuid) {
		String addonVersion = AddonUtil.getVersion();

		if (!Config.has("version")) {
			// Starting for the first time -> Not updated
			Config.set("version", new JsonPrimitive(addonVersion));
			Config.save();
			return;
		}

		check(addonUuid);

		if (Config.get("version").getAsString().equals(addonVersion))
			return;

		Config.set("version", new JsonPrimitive(addonVersion));
		Config.save();
	}

	private static boolean triggeredShutdownHook = false;
	private static boolean isUpToDate = true;

	public static boolean isUpToDate() {
		return isUpToDate;
	}

	private static void check(UUID addonUuid) {
		if (!enabled.get())
			return;

		File currentAddonJar = AddonLoader.getFiles().get(addonUuid);
		if (currentAddonJar == null) {
			// Probably in dev environment, skip updating
			return;
		}

		IOUtil.read("https://grieferutils.l3g7.dev/v2/latest_release/version").asJsonString(latestVersion -> {
			if (latestVersion.isEmpty())
				return;

			if (latestVersion.equalsIgnoreCase(AddonUtil.getVersion())) {
				if (!triggeredShutdownHook) {
					Runtime.getRuntime().addShutdownHook(new Thread(() -> check(addonUuid)));
					triggeredShutdownHook = true;
				}
				return;
			}

			try  {
				// Download new version
				HttpURLConnection conn = (HttpURLConnection) new URL("https://grieferutils.l3g7.dev/v2/latest_release/jar").openConnection();
				conn.addRequestProperty("User-Agent", "GrieferUtils");

				File newAddonJar = new File(AddonLoader.getAddonsDirectory(), "griefer-utils-v" + latestVersion + ".jar");
				Files.copy(conn.getInputStream(), newAddonJar.toPath(), REPLACE_EXISTING);

				// Add old version to LabyMod's .delete
				Path deleteFilePath = AddonLoader.getDeleteQueueFile().toPath();
				String deleteLine = currentAddonJar.getName() + System.lineSeparator();
				Files.write(deleteFilePath, deleteLine.getBytes(), CREATE, APPEND);

				isUpToDate = false;

			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}


}