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

package dev.l3g7.griefer_utils.features.uncategorized.settings.debug.log.log_entries;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.features.uncategorized.settings.debug.log.LogEntry;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.minecraft.world.chunk.IChunkProvider;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static dev.l3g7.griefer_utils.core.util.JsonUtil.jsonObject;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.world;
import static java.nio.charset.StandardCharsets.UTF_8;

public class WorldMetadataEntry extends LogEntry {

	@MainElement
	private static final BooleanSetting enabled = new BooleanSetting()
		.name("Welt-Metadaten")
		.config("settings.debug.log.world_metadata")
		.defaultValue(true)
		.custom("Ja", "Nein")
		.icon("chunk");

	@Override
	public void addEntry(ZipOutputStream zip) throws IOException {
		zip.putNextEntry(new ZipEntry("world_metadata.json"));

		if (world() == null) {
			zip.write("\"not ingame\"".getBytes(UTF_8));
			zip.closeEntry();
			return;
		}

		IChunkProvider provider = world().getChunkProvider();
		JsonObject obj = jsonObject(
			"chunkProvider", jsonObject(
				"instance", provider.toString(),
				"type", provider.getClass().toString(),
				"loadedChunkCount", provider.getLoadedChunkCount(),
				"str", provider.makeString()
			),
			"entities", jsonObject(
				"entityCount", world().loadedEntityList.size(),
				"tileEntityCount", world().loadedTileEntityList.size(),
				"playerCount", world().playerEntities.size()
			),
			"remote", world().isRemote,
			"instance", world().toString(),
			"type", world().getClass().toString()
		);
		try {
			obj.addProperty("server", world().getScoreboard().getTeam("server_value").getColorPrefix());
		} catch (NullPointerException e) {
			obj.addProperty("server", "<null>");
		}

		zip.write(obj.toString().getBytes(UTF_8));
		zip.closeEntry();
	}
}