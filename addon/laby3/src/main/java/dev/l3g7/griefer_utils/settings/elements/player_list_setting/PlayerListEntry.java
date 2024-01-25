/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.settings.elements.player_list_setting;

import dev.l3g7.griefer_utils.core.misc.Constants;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.core.XboxProfileResolver;
import net.labymod.main.ModTextures;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;

import java.io.IOException;

import static dev.l3g7.griefer_utils.settings.elements.player_list_setting.PlayerListEntryResolver.LOOKUP_MAP;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

public class PlayerListEntry {

	public static final PlayerListEntry INVALID_PLAYER = new PlayerListEntry();

	public static PlayerListEntry getEntry(String name) {
		if (!Constants.UNFORMATTED_PLAYER_NAME_PATTERN.matcher(name).matches())
			return INVALID_PLAYER;

		return LOOKUP_MAP.computeIfAbsent(name, k -> new PlayerListEntry(name, null));
	}

	public String name;
	public String id; // The uuid / xuid of the Player
	public boolean oldSkin;
	public ITextureObject skin = null;
	public boolean loaded = false; // Whether the entry's name and id were loaded
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
				if (exists) {
					try {
						PlayerListEntryResolver.loadFromXbox(this);
					} catch (IOException e) {
						PlayerListEntryResolver.loadFromPlayerDB(this);
					}
				}
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

	public void renderSkull(double x, double y, int size) {
		if (skin == null) {
			mc().getTextureManager().bindTexture(ModTextures.MISC_HEAD_QUESTION);
			drawUtils().drawTexture(x, y, 0, 0, 256, 256, size, size);
			return;
		}

		GlStateManager.bindTexture(skin.getGlTextureId());

		if (!isMojang()) {
			drawUtils().drawTexture(x, y, 0, 0, 256, 256, size, size);
			return;
		}

		int yHeight = oldSkin ? 64 : 32; // Old textures are 32x64
		drawUtils().drawTexture(x, y, 32, yHeight, 32, yHeight, size, size); // First layer
		drawUtils().drawTexture(x, y, 160, yHeight, 32, yHeight, size, size); // Second layer
	}

}
