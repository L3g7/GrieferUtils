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

package dev.l3g7.griefer_utils.v1_8_9.bridges;

import com.sun.jna.platform.win32.Crypt32Util;
import dev.l3g7.griefer_utils.api.bridges.Bridge;
import dev.l3g7.griefer_utils.api.bridges.MinecraftBridge;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.v1_8_9.misc.ChatQueue;
import dev.l3g7.griefer_utils.v1_8_9.misc.ServerCheck;
import dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.launchwrapper.Launch;

import java.io.File;
import java.util.UUID;

@Singleton
@Bridge
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
		return Minecraft.getMinecraft().getSession().getProfile().getId();
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
