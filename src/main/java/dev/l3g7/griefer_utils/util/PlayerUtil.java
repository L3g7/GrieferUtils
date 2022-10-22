/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.util;

import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.UUID;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.StaticImport.mc;

/**
 * A utility class for minecraft player related methods.
 */
public class PlayerUtil {

	/**
	 * @return the player's real name.
	 */
	public static String unnick(String nickedName) {
		for(NetworkPlayerInfo info : mc().getNetHandler().getPlayerInfoMap()) {
			if(info.getDisplayName() != null) {
				String[] parts = info.getDisplayName().getUnformattedText().split("\u2503");
				if(parts.length > 1 && parts[1].trim().equals(nickedName))
					return info.getGameProfile().getName();
			}
		}
		return nickedName;
	}

	/**
	 * @return the player's uuid.
	 */
	public static UUID getUUID(String name) {
		for(NetworkPlayerInfo info : mc().getNetHandler().getPlayerInfoMap()) {
			if(info.getGameProfile().getName().equals(name))
				return info.getGameProfile().getId();

			if(info.getDisplayName() != null) {
				String[] parts = info.getDisplayName().getUnformattedText().split("\u2503");
				if(parts.length > 1 && parts[1].trim().equals(name))
					return info.getGameProfile().getId();
			}
		}
		return null;
	}

}
