/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.laby4;

import dev.l3g7.griefer_utils.auto_update.AutoUpdater;
import dev.l3g7.griefer_utils.auto_update.UpdateImpl;
import net.labymod.api.Constants;
import net.labymod.api.addon.entrypoint.Entrypoint;
import net.labymod.api.addon.exception.AddonLoadException;
import net.labymod.api.models.addon.annotation.AddonEntryPoint;
import net.labymod.api.models.version.Version;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static dev.l3g7.griefer_utils.auto_update.AutoUpdater.DELETION_MARKER;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

@AddonEntryPoint(priority = 900)
@SuppressWarnings("UnstableApiUsage")
public class PreStart implements Entrypoint, UpdateImpl {

	@Override
	public void initialize(Version semVer) {
		if (System.setProperty("griefer_utils_load_flag", "") != null)
			throw new Error("GrieferUtils wurde bereits geladen!");

		AutoUpdater.update(this);
		EarlyStart.start(semVer);
	}

	@Override
	public void deleteJar(File jar) throws IOException {
		// Try to delete file directly
		if (jar.delete())
			return;

		// Minecraft's ClassLoader can create file leaks so the jar is probably locked.

		// Prepare jar for deletion by LabyMod
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ZipOutputStream out = new ZipOutputStream(bout);
		out.setComment(DELETION_MARKER);
		out.putNextEntry(new ZipEntry("addon.json"));
		out.write("{\"namespace\":\"griefer_utils-marked_for_deletion\",\"author\":\"\",\"displayName\":\"\",\"version\":\"\",\"compatibleMinecraftVersions\":\"\",\"meta\":[]}".getBytes());
		out.close();
		Files.write(jar.toPath(), bout.toByteArray());

		// Add jar to .asfr
		String deleteLine = "griefer_utils-marked_for_deletion" + System.lineSeparator();
		Files.write(Constants.Files.ADDONS_SCHEDULE_FOR_REMOVAL, deleteLine.getBytes(), CREATE, APPEND);
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
