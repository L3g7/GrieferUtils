/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

		Bridge.Version mcVersion = Bridge.Version.getMinecraftBySemVer(semVer.toString());
		if (mcVersion == null)
			throw new UnsupportedAddonException("GrieferUtils ist nicht für Version " + semVer + " verfügbar!");

		Bridge.Initializer.init(Bridge.Version.LABY_4, mcVersion);
		EarlyStart.start();
	}

}
