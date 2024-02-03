/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.l3g7.griefer_utils.auto_update.AutoUpdater;
import dev.l3g7.griefer_utils.auto_update.UpdateInfoProvider;
import net.labymod.api.Constants;
import net.labymod.api.addon.entrypoint.Entrypoint;
import net.labymod.api.addon.exception.AddonLoadException;
import net.labymod.api.models.addon.annotation.AddonEntryPoint;
import net.labymod.api.models.version.Version;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.jar.JarFile;

@AddonEntryPoint(priority = 1001)
@SuppressWarnings("UnstableApiUsage")
public class PreStart implements Entrypoint, UpdateInfoProvider {

	@Override
	public void initialize(Version semVer) {
		if (System.setProperty("griefer_utils_load_flag", "") != null)
			throw new Error("GrieferUtils wurde bereits geladen!");

		AutoUpdater.update(this);
		EarlyStart.start(semVer);
	}

	@Override
	public Path getDeletionList() {
		return Constants.Files.ADDONS_SCHEDULE_FOR_REMOVAL;
	}

	@Override
	public String getDeletionEntry(File fileToBeDeleted) throws IOException {
		try (JarFile file = new JarFile(fileToBeDeleted)) {
			JsonElement addonJson = JsonParser.parseReader(new InputStreamReader(file.getInputStream(file.getEntry("addon.json"))));
			return addonJson.getAsJsonObject().get("namespace").getAsString();
		}
	}

	@Override
	public void handleError(Throwable e) {
		if (e instanceof IOException) {
			// Allow start if updating failed due to network errors
			e.printStackTrace(System.err);
		} else
			throw new AddonLoadException("Could not update GrieferUtils!", e);
	}

}
