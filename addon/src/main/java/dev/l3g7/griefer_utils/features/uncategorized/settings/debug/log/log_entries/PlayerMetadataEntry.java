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
import com.mojang.authlib.properties.PropertyMap;
import dev.l3g7.griefer_utils.features.uncategorized.settings.debug.log.LogEntry;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.utils.JsonParse;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Session;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static dev.l3g7.griefer_utils.core.util.JsonUtil.jsonObject;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.player;
import static java.nio.charset.StandardCharsets.UTF_8;

public class PlayerMetadataEntry extends LogEntry {

	@MainElement
	private static final BooleanSetting enabled = new BooleanSetting()
		.name("Spieler-Metadaten")
		.config("settings.debug.log.player_metadata")
		.defaultValue(true)
		.custom("Ja", "Nein")
		.icon("steve");

	@Override
	public void addEntry(ZipOutputStream zip) throws IOException {
		zip.putNextEntry(new ZipEntry("player_metadata.json"));

		// Session metadata
		Session sess = MinecraftUtil.mc().getSession();
		JsonObject main = jsonObject(
			"session", jsonObject(
				"username", sess.getUsername(),
				"playerId", sess.getPlayerID(),
				"type", nullSafeOp(sess.getSessionType(), Enum::toString),
				"properties", new PropertyMap.Serializer().serialize(sess.getProfile().getProperties(), null, null)
			)
		);

		// Ingame player metadata
		if (player() != null) {
			main.add("player", jsonObject(
				"posX", player().posX,
				"posy", player().posY,
				"posZ", player().posZ,
				"instance", player().toString(),
				"type", player().getClass().toString(),
				"name", player().getName(),
				"customName", player().getCustomNameTag(),
				"displayName", JsonParse.parse(IChatComponent.Serializer.componentToJson(player().getDisplayName()))
			));
		}

		zip.write(main.toString().getBytes(UTF_8));
		zip.closeEntry();
	}
}