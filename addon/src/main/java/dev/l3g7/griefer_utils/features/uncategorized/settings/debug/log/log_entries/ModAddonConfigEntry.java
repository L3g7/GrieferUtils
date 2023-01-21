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

import dev.l3g7.griefer_utils.features.uncategorized.settings.debug.log.LogEntry;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import net.labymod.addon.AddonLoader;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

public class ModAddonConfigEntry extends LogEntry {

	@MainElement
	private static final BooleanSetting enabled = new BooleanSetting()
		.name("Mod- & Addon-Config")
		.config("settings.debug.log.mod_addon_config")
		.defaultValue(true)
		.custom("Ja", "Nein")
		.icon("cpu");

	@Override
	public void addEntry(ZipOutputStream zip) throws IOException {
		for (File config : AddonLoader.getConfigDirectory().listFiles())
			includeFile(zip, config, "addon_configs/" + config.getName());

		for (File config : new File(Minecraft.getMinecraft().mcDataDir, "config").listFiles())
			includeFile(zip, config, "mod_configs/" + config.getName());
	}
}