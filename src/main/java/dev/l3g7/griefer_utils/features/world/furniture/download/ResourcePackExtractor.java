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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static dev.l3g7.griefer_utils.util.misc.Constants.FURNITURE_RESOURCE_PACK_DIR;

public class ResourcePackExtractor {

	public static void extractResourcePack(File jarFile) throws IOException {
		if (!FURNITURE_RESOURCE_PACK_DIR.exists())
			if (!FURNITURE_RESOURCE_PACK_DIR.mkdirs())
				throw new IOException("Failed to create resource pack directory!");

		ZipInputStream zis = new ZipInputStream(Files.newInputStream(jarFile.toPath()));

		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			extractEntry(entry, zis);
			zis.closeEntry();
		}

		zis.close();
	}

	private static void extractEntry(ZipEntry entry, ZipInputStream zis) throws IOException {
		if (!entry.getName().startsWith("assets/") && !entry.getName().startsWith("customblocks/"))
			return;

		File dest = new File(FURNITURE_RESOURCE_PACK_DIR, entry.getName());

		if (entry.isDirectory()) {
			if (!dest.mkdirs())
				throw new IOException("Failed to create directory: " + entry.getName());

			return;
		}

		byte[] buffer = new byte[1024];

		try (FileOutputStream fos = new FileOutputStream(dest)) {
			int len;
			while ((len = zis.read(buffer)) > 0)
				fos.write(buffer, 0, len);
		}
	}

}
