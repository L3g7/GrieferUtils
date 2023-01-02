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

import com.google.gson.*;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.AddonUtil;
import dev.l3g7.griefer_utils.util.IOUtil;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.misc.Config;
import net.labymod.addon.AddonLoader;
import net.minecraft.init.Blocks;

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

	public static final BooleanSetting showChangelog = new BooleanSetting()
		.name("Changelog anzeigen")
		.description("Ob der Changelog angezeigt werden soll, wenn GrieferUtils aktualisiert wurde.")
		.config("settings.auto_update.show_changelog")
		.icon(ItemUtil.createItem(Blocks.stained_glass_pane, 12, true))
		.defaultValue(true);

	public static final BooleanSetting enabled = new BooleanSetting()
		.name("Automatisch updaten")
		.description("Updatet GrieferUtils automatisch auf die neuste Version.")
		.config("settings.auto_update.enabled")
		.icon("arrow_circle")
		.defaultValue(true)
		.subSettings(showChangelog);

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

		if (showChangelog.get() && Config.get("version").getAsString().startsWith("1."))
			ChangelogScreen.trigger();
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

		IOUtil.read("https://grieferutils.l3g7.dev/v2/latest_release").asJsonString(releaseId -> {
			if (releaseId.isEmpty())
				return;

			IOUtil.read("https://api.github.com/repos/L3g7/GrieferUtils/releases/" + releaseId).asJsonObject(latestRelease -> {
				String tag = latestRelease.get("tag_name").getAsString().replaceFirst("v", "");
				if (tag.equals(AddonUtil.getVersion())) {
					if (!triggeredShutdownHook) {
						Runtime.getRuntime().addShutdownHook(new Thread(() -> check(addonUuid)));
						triggeredShutdownHook = true;
					}
					return;
				}

				// Get latest addon asset
				JsonArray assets = latestRelease.get("assets").getAsJsonArray();
				JsonObject asset = null;

				for (JsonElement jsonElement : assets) {
					JsonObject currentAsset = jsonElement.getAsJsonObject();
					if (currentAsset.get("name").getAsString().equals("griefer-utils-v" + tag + ".jar")) {
						asset = currentAsset;
						break;
					}
				}

				if (asset == null) {
					System.err.println("No correct GrieferUtils release could be found");
					return;
				}

				String downloadUrl = asset.get("browser_download_url").getAsString();

				HttpURLConnection conn;

				try {
					// Download new version
					conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
					conn.addRequestProperty("User-Agent", "GrieferUtils");

					File newAddonJar = new File(AddonLoader.getAddonsDirectory(), asset.get("name").getAsString());
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
		});
	}


}