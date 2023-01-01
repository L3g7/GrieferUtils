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

import dev.l3g7.griefer_utils.util.IOUtil;
import dev.l3g7.griefer_utils.util.misc.Constants;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.function.Consumer;

public class AddonDownloader {

	public static void downloadAddon(Consumer<File> consumer, Consumer<Exception> exceptionConsumer) {
		IOUtil.read("https://grieferutils.l3g7.dev/v2/furniture/metadata").asJsonObject(jsonObject -> {
			String uuid = jsonObject.get("uuid").getAsString();
			String hash = jsonObject.get("hash").getAsString();

			File mmFile = new File(Minecraft.getMinecraft().mcDataDir, "MysteryMod/addons/1.8.9/Furniture.jar");
			if (sha512(mmFile).equals(hash)) {
				consumer.accept(mmFile);
				return;
			}

			File tempFile;
			try {
				tempFile = Files.createTempFile("GrieferUtilsFurnitureAddonFile_", ".jar").toFile();
			} catch (IOException e) {
				e.printStackTrace();
				exceptionConsumer.accept(e);
				return;
			}

			IOUtil.read(String.format(Constants.MM_ADDON_DOWNLOAD_URL, uuid))
				.asFile(tempFile, file -> {
					if (!sha512(file).equals(hash))
						throw new RuntimeException("Official furniture addon has an invalid hash: " + sha512(tempFile));

					consumer.accept(file);
				});
		});
	}

	private static String sha512(File file) {
		if (!file.isFile())
			return "";

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			byte[] hash = md.digest(Files.readAllBytes(file.toPath()));
			return Base64.getEncoder().encodeToString(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-512 does not exist, this shouldn't be possible");
		} catch (IOException e) {
			return "";
		}
	}

}
