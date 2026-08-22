/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.settings.types.list.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.misc.Constants;
import dev.l3g7.griefer_utils.core.settings.types.list.ListEntry;
import net.minecraft.client.renderer.texture.ITextureObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class PlayerListEntry implements ListEntry<PlayerListEntry> {

	private final static Pattern UUID_PATTERN = Pattern.compile("^[\\da-f]{8}-(?:[\\da-f]{4}-){3}[\\da-f]{12}$");
	private final static Pattern UUID_COMPACT_PATTERN = Pattern.compile("^[\\da-f]{32}$");

	protected static final Map<String, PlayerListEntry> NAME_LOOKUP_MAP = new ConcurrentHashMap<>();
	protected static final Map<String, PlayerListEntry> UUID_LOOKUP_MAP = new ConcurrentHashMap<>();
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
	protected boolean isValid = false;
	protected boolean isLoaded;

	private PlayerListEntry() {
		isLoaded = true;
	}

	protected PlayerListEntry(String name, String id) {
		this.name = name;
		this.id = id;
		this.isLoaded = false;
		Resolver.resolve(this);
	}

	public static PlayerListEntry fromName(String name) {
		if (!Constants.UNFORMATTED_PLAYER_NAME_PATTERN.matcher(name).matches())
			return PlayerListEntry.INVALID_PLAYER;

		return NAME_LOOKUP_MAP.computeIfAbsent(name, k -> new PlayerListEntry(name, null));
	}

	public static PlayerListEntry fromUUID(String uuid) {
		uuid = uuid.toLowerCase().trim();
		if (UUID_COMPACT_PATTERN.matcher(uuid).matches())
			uuid = uuid.replaceAll("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3-$4-$5");
		else if (!UUID_PATTERN.matcher(uuid).matches())
			return PlayerListEntry.INVALID_PLAYER;

		String id = uuid;
		return UUID_LOOKUP_MAP.computeIfAbsent(uuid, k -> new PlayerListEntry(null, id));
	}

	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name == null ? "§cNutzer konnte nicht geladen werden!" : name;
	}

	public boolean isJava() {
		return id == null ? !name.startsWith("!") : id.contains("-");
	}

	public int skinHeight() {
		return slim ? 64 : 32;
	}

	public ITextureObject skin() {
		return skin;
	}

	public boolean isValid() {
		return isValid;
	}

	public boolean isLoaded() {
		return isLoaded;
	}

	@Override
	public PlayerListEntry createNew() {
		return new PlayerListEntry();
	}

	public void copyFrom(PlayerListEntry entry) {
		this.id = entry.id;
		this.name = entry.name;
		this.slim = entry.slim;
		this.skin = entry.skin;
		this.isValid = entry.isValid;
		this.isLoaded = entry.isLoaded;
	}

	@Override
	public void load(JsonElement data) {
		this.name = null;
		this.id = data.getAsString();
		this.isLoaded = false;
		Resolver.resolve(this);
	}

	@Override
	public JsonElement encode() {
		return new JsonPrimitive(this.id);
	}

}
