/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4;

import dev.l3g7.griefer_utils.core.auto_update.AutoUpdater;
import net.labymod.api.Constants;
import net.labymod.api.models.addon.annotation.AddonEntryPoint;
import net.labymod.api.models.version.Version;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static dev.l3g7.griefer_utils.core.auto_update.AutoUpdater.DELETION_MARKER;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

@AddonEntryPoint(priority = 900)
@SuppressWarnings("UnstableApiUsage")
public class Init implements net.labymod.api.addon.entrypoint.Entrypoint, AutoUpdater.Init {

	@Override
	public void initialize(Version semVer) {
		if (System.setProperty("griefer_utils_load_flag", "") != null)
			throw new Error("GrieferUtils wurde bereits geladen!");

		if (!AutoUpdater.update(this))
			new Entrypoint().start();
	}

	@Override
	public void forceDeleteJar(File jar) throws IOException {
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
	public String getLabyVersion() {
		return "laby4";
	}

}
