/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.bridges;

import com.sun.jna.platform.win32.Crypt32Util;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.MinecraftBridge;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.ChatQueue;
import dev.l3g7.griefer_utils.core.misc.ServerCheck;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.launchwrapper.Launch;

import java.io.File;
import java.util.UUID;

@Bridge
@Singleton
public class MinecraftBridgeImpl implements MinecraftBridge {

	@Override
	public ClassLoader launchClassLoader() {
		return Launch.classLoader;
	}

	@Override
	public void scale(float x, float y, float z) {
		GlStateManager.scale(x, y, z);
	}

	@Override
	public File assetsDir() {
		return Launch.assetsDir;
	}

	@Override
	public UUID uuid() {
		return MinecraftUtil.uuid();
	}

	@Override
	public byte[] winCryptUnprotectData(byte[] data) {
		return Crypt32Util.cryptUnprotectData(data);
	}

	@Override
	public void send(String message) {
		ChatQueue.send(message);
	}

	@Override
	public boolean onGrieferGames() {
		return ServerCheck.isOnGrieferGames();
	}

	@Override
	public String getGrieferGamesSubServer() {
		return MinecraftUtil.getServerFromScoreboard();
	}

}
