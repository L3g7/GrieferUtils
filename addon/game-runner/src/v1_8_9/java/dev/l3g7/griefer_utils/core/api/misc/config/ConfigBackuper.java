/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.config;

import dev.l3g7.griefer_utils.core.api.util.Util;

import java.io.IOException;
import java.nio.file.*;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ConfigBackuper {

	public static void backup(String prevVersion) {
		try {
			backup(Config.configFile, prevVersion);
		} catch (IOException e) {
			throw Util.elevate(e, "Could not create backup of config file!");
		}
	}

	private static void backup(Path configFile, String prevVersion) throws IOException {
		Path tempPath = Files.createTempFile("GrieferUtils-backup", ".json");
		Files.copy(configFile, tempPath, REPLACE_EXISTING);

		String fileName = "GrieferUtils-" + prevVersion + "-to-" + labyBridge.addonVersion();
		int id = 0;

		Path backupPath;
		while (true) {
			id++; // Start at ID 1
			backupPath = Paths.get("GrieferUtils", "backups", fileName + "-#" + id + ".json");

			if (!Files.exists(backupPath) && tryMove(tempPath, backupPath))
				return;
		}
	}

	/**
	 * Atomically moves source to destination, aborting if the file already exists.
	 *
	 * @return true if the file was moved, false if it already exists.
	 */
	public static boolean tryMove(Path source, Path destination) throws IOException {
		try {
			Files.createDirectories(destination.getParent());
			try {
				Files.move(source, destination, ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(source, destination);
			}
			return true;
		} catch (FileAlreadyExistsException e) {
			return false;
		}
	}

}
