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

import com.google.gson.JsonArray;
import dev.l3g7.griefer_utils.features.uncategorized.settings.debug.log.LogEntry;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.addon.AddonLoader;
import net.labymod.addon.online.AddonInfoManager;
import net.labymod.addon.online.info.AddonInfo;
import net.labymod.settings.LabyModAddonsGui;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static dev.l3g7.griefer_utils.util.JsonUtil.jsonObject;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ModAddonMetadataEntry extends LogEntry {

	@MainElement
	private static final BooleanSetting enabled = new BooleanSetting()
		.name("Mod- & Addon-Metadaten")
		.config("settings.debug.log.mod_addon_metadata")
		.defaultValue(true)
		.custom("Ja", "Nein")
		.icon("cpu");

	@Override
	public void addEntry(ZipOutputStream zip) throws IOException {
		zip.putNextEntry(new ZipEntry("mod_addon_metadata.json"));

		JsonArray mods = new JsonArray();
		for (ModContainer mod : Loader.instance().getModList()) {
			ModMetadata meta = mod.getMetadata();
			mods.add(jsonObject(
				"modId", mod.getModId(),
				"name", mod.getName(),
				"version", mod.getVersion(),
				"author", meta.credits,
				"description", meta.description,
				"mainClass", nullSafeOp(mod.getMod(), o -> o.getClass().toString()),
				"file", nullSafeOp(mod.getSource(), File::getName)
			));
		}

		JsonArray addons = new JsonArray();
		for (AddonInfo info : AddonInfoManager.getInstance().getAddonInfoList()) {
			if (!AddonLoader.hasInstalled(info))
				continue;

			addons.add(jsonObject(
				"uuid", info.getUuid(),
				"name", info.getName(),
				"version", info.getVersion(),
				"author", info.getAuthor(),
				"description", info.getDescription(),
				"category", info.getCategory(),
			"mainClass", nullSafeOp(AddonLoader.getAddonByUUID(info.getUuid()), o -> o.getClass().toString()),
				"file", nullSafeOp(AddonLoader.getFiles().get(info.getUuid()), File::getName)
			));
		}

		zip.write(jsonObject("mods", mods, "addons", addons).toString().getBytes(UTF_8));
		zip.closeEntry();
	}
}