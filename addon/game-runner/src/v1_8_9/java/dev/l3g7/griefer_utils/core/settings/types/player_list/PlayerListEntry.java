/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.settings.types.player_list;

import dev.l3g7.griefer_utils.core.api.misc.ThreadFactory;
import dev.l3g7.griefer_utils.core.api.misc.xbox_profile_resolver.core.XboxProfileResolver;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.texture.ITextureObject;

import java.io.IOException;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;
import static java.lang.Thread.MAX_PRIORITY;

public class PlayerListEntry {

	public static final PlayerListEntry INVALID_PLAYER = new PlayerListEntry();

	/**
	 * The uuid / xuid of the player
	 */
	protected String id;
	protected String name;
	protected boolean slim;
	protected ITextureObject skin = null;

	/**
	 * True if the entry's name and id are set.
	 */
	protected boolean loaded = false;
	protected boolean exists = true;

	private PlayerListEntry() {
		exists = false;
	}

	public PlayerListEntry(String name, String id) {
		this.id = id;
		this.name = name;
		load();
	}

	public String getId() {
		return id;
	}

	public String name() {
		return name;
	}

	public boolean isMojang() {
		return id == null ? !name.startsWith("!") : id.contains("-");
	}

	public int skinHeight() {
		return slim ? 64 : 32;
	}

	public ITextureObject skin() {
		return skin;
	}

	public boolean loaded() {
		return loaded;
	}

	public boolean exists() {
		return exists;
	}

	private void load() {
		if (!isMojang()) {
			ThreadFactory.run("Grieferutils PlayerListEntry Resolver", MAX_PRIORITY, () -> {
				if (!exists || !XboxProfileResolver.isAvailable())
					PlayerListEntryResolver.loadFromPlayerDB(this);
				if (exists) {
					try {
						PlayerListEntryResolver.loadFromXbox(this);
					} catch (IOException e) {
						PlayerListEntryResolver.loadFromPlayerDB(this);
					}
				}
			});
			return;
		}

		// Try to load the uuid it from tab list
		if (mc().getNetHandler() != null) {
			for (NetworkPlayerInfo info : mc().getNetHandler().getPlayerInfoMap())
				if (info.getGameProfile().getName().equals(name))
					id = info.getGameProfile().getId().toString();
		}

		ThreadFactory.run("Grieferutils PlayerListEntry Resolver", MAX_PRIORITY, () -> {
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
		});
	}


}
