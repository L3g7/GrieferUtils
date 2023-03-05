/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.settings.elements.player_list_setting;

import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.core.XboxProfileResolver;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.texture.ITextureObject;

import java.io.IOException;

import static dev.l3g7.griefer_utils.settings.elements.player_list_setting.PlayerListEntryResolver.LOOKUP_MAP;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

public class PlayerListEntry {

	private static final PlayerListEntry INVALID_PLAYER = new PlayerListEntry();

	public static PlayerListEntry getEntry(String name) {
		if (!Constants.UNFORMATTED_PLAYER_NAME_PATTERN.matcher(name).matches())
			return INVALID_PLAYER;

		return LOOKUP_MAP.computeIfAbsent(name, k -> new PlayerListEntry(name, null));
	}

	protected String name;
	protected String id; // The uuid / xuid of the Player
	protected boolean slim;
	protected ITextureObject skin = null;
	protected boolean loaded = false; // Whether the entry's name and id were loaded
	protected boolean exists = true;

	private PlayerListEntry() {
		exists = false;
	}

	public PlayerListEntry(String name, String id) {
		this.name = name;
		this.id = id;
		load();
	}

	public boolean isMojang() {
		return id == null ? !name.startsWith("!") : id.contains("-");
	}

	private void load() {
		if (!isMojang()) {
			new Thread(() -> {
				if (!exists || !XboxProfileResolver.isAvailable())
					PlayerListEntryResolver.loadFromPlayerDB(this);
				if (exists)
					PlayerListEntryResolver.loadFromXbox(this);
			}).start();
			return;
		}

		// Try to load the uuid it from tab list
		if (mc().getNetHandler() != null) {
			for (NetworkPlayerInfo info : mc().getNetHandler().getPlayerInfoMap()) {
				if (info.getGameProfile().getName().equals(name)) {
					id = info.getGameProfile().getId().toString();
				}
			}
		}

		new Thread(() -> {
			try {
				PlayerListEntryResolver.loadFromMojang(this);
			} catch (IOException e1) {
				try {
					PlayerListEntryResolver.loadFromAshcon(this);
				} catch (IOException e2) {
					e1.printStackTrace();
					e2.printStackTrace();
				}
			}
		}).start();
	}


	@Override
	public String toString() {
		return "PlayerListEntry{" +
			"name='" + name + '\'' +
			", id='" + id + '\'' +
			", loaded=" + loaded +
			", exists=" + exists +
			'}';
	}
}
