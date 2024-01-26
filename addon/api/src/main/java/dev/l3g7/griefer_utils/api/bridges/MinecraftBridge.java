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
