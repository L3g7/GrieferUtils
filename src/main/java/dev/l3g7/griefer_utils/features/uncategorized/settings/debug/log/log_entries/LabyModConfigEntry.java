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

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

public class LabyModConfigEntry extends LogEntry {

	@MainElement
	private static final BooleanSetting enabled = new BooleanSetting()
		.name("LabyMod-Config")
		.config("settings.debug.log.laby_mod_config")
		.defaultValue(true)
		.custom("Ja", "Nein")
		.icon("labymod:labymod_logo");

	@Override
	public void addEntry(ZipOutputStream zip) throws IOException {
		includeFile(zip, new File("LabyMod/LabyMod-3.json"), "labymod_config.json");
	}
}