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

package dev.l3g7.griefer_utils.features.world.furniture.download;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.Consumer;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.displayAchievement;
import static dev.l3g7.griefer_utils.util.misc.Constants.FURNITURE_RESOURCE_PACK_DIR;

public class AssetsCreator {

	public static boolean assetsExist() {
		return new File(FURNITURE_RESOURCE_PACK_DIR, "customblocks/default_blocks.json").isFile();
	}

	public static void createAssets() {
		createAssets(e -> {
			if (e == null) {
				displayAchievement(null, "§e§lStarte Minecraft neu", "§eUm die Möbel zu aktivieren, muss Minecraft neu gestartet werden.");
				return;
			}

			e.printStackTrace();
			if (e instanceof RuntimeException) { // Hash is invalid
				displayAchievement(null, "§c§lFataler Fehler \u26A0", "§cEin Fehler ist aufgetreten. Bitte melde dich beim Team.");
			} else {
				displayAchievement(null, "§c§lFehler \u26A0", "§cBitte versuche es erneut und melde dich beim Team, wenn er wieder auftritt.");
			}
		});
	}

	public static void createAssets(Consumer<Exception> onFinished) {
		if (assetsExist()) {
			onFinished.accept(null);
			return;
		}

		AddonDownloader.downloadAddon(file -> {
			try {
				ResourcePackExtractor.extractResourcePack(file);
				ResourceRewriter.rewrite();
				file.deleteOnExit();

				// Create pack.mcmeta
				byte[] bytes = "{\"pack\":{\"pack_format\":1,\"description\":\"\"}}".getBytes(StandardCharsets.UTF_8);
				Files.write(new File(FURNITURE_RESOURCE_PACK_DIR, "pack.mcmeta").toPath(), bytes);
			} catch (IOException e) {
				onFinished.accept(e);
			}

			onFinished.accept(null);
		}, onFinished);
	}

}
