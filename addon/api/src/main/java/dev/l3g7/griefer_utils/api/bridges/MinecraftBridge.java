/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.api.bridges;

import dev.l3g7.griefer_utils.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.api.file_provider.FileProvider;

import java.io.File;
import java.util.UUID;

@Bridged
public interface MinecraftBridge {

	MinecraftBridge minecraftBridge = FileProvider.getBridge(MinecraftBridge.class);

	ClassLoader launchClassLoader();

	void scale(float x, float y, float z);

	File assetsDir();

	UUID uuid();

	byte[] winCryptUnprotectData(byte[] data);

	void send(String message);

	boolean onGrieferGames();

	String getGrieferGamesSubServer();

	interface McItemStack {}

}
