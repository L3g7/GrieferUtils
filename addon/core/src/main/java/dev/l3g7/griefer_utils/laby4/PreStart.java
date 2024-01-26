/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.laby4;

import dev.l3g7.griefer_utils.api.bridges.Bridge;
import net.labymod.api.addon.entrypoint.Entrypoint;
import net.labymod.api.addon.exception.UnsupportedAddonException;
import net.labymod.api.models.addon.annotation.AddonEntryPoint;
import net.labymod.api.models.version.Version;

@AddonEntryPoint(priority = 1001)
@SuppressWarnings("UnstableApiUsage")
public class PreStart implements Entrypoint {

	@Override
	public void initialize(Version semVer) {
		if (System.setProperty("griefer_utils_load_flag", "") != null)
			throw new Error("GrieferUtils wurde bereits geladen!");

		String version = semVer.toString().replace('.', '_');
		if (!Bridge.Initializer.init("laby4", "v" + version))
			throw new UnsupportedAddonException("GrieferUtils ist nicht für Version " + semVer + " verfügbar!");

		try {
			EarlyStart.start();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

}
