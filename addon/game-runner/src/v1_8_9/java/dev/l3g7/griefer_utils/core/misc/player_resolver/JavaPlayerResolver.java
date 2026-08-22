/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.player_resolver;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.util.StringUtil;
import dev.l3g7.griefer_utils.core.api.util.io.HttpGetOperation;
import dev.l3g7.griefer_utils.core.api.util.io.IO;

import java.util.Base64;

import static dev.l3g7.griefer_utils.core.misc.player_resolver.PlayerResolver.Result.FOUND;
import static dev.l3g7.griefer_utils.core.misc.player_resolver.PlayerResolver.Result.tryLoad;

/**
 * Resolution strategy:
 * 1. Resolve using official Mojang API
 * 2. If this fails, resolve using mc-api.io
 * 3. If this fails, resolve using playerdb.co
 * 4. If this fails, resolve using laby.net
 * 5. If this fails, resolve using api.ashcon.app
 */
class JavaPlayerResolver extends PlayerResolver {

	protected static Result load(PlayerListEntry entry) {
		return Result.get(() -> {
			tryLoadFromMojang(entry);
			tryLoadFromMcAPI(entry);
			tryLoadFromPlayerDB(entry);
			tryLoadFromLabyNet(entry);
			return loadFromAshcon(entry);
		});
	}

	private static void tryLoadFromMojang(PlayerListEntry entry) {
		tryLoad(() -> {
			// Resolve UUID
			if (entry.id == null) {
				HttpGetOperation op = IO.read("https://api.mojang.com/users/profiles/minecraft/" + entry.name);
				checkHttpCode(op);

				JsonObject data = op.asJsonObject();
				entry.id = StringUtil.normalizeUUID(data.get("id").getAsString());
			}

			// Resolve name, texture
			JsonObject profile = IO.read("https://sessionserver.mojang.com/session/minecraft/profile/" + entry.id).asJsonObject();
			entry.name = profile.get("name").getAsString();

			for (JsonElement element : profile.getAsJsonArray("properties")) {
				JsonObject property = element.getAsJsonObject();

				if (!property.get("name").getAsString().equals("textures"))
					continue;

				JsonObject data = IO.read(Base64.getDecoder().decode(property.get("value").getAsString())).asJsonObject();
				String url = data.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
				loadSkin(entry, IO.read(url).asBytes());
			}
		});
	}

	private static void tryLoadFromMcAPI(PlayerListEntry entry) {
		tryLoad(() -> {
			HttpGetOperation op = entry.id == null
				? IO.read("https://mc-api.io/profile/" + entry.name + "/JAVA")
				: IO.read("https://mc-api.io/profile/" + entry.id);

			checkHttpCode(op);

			JsonObject profile = op.asJsonObject();
			entry.name = profile.get("name").getAsString();
			entry.id = profile.get("uuid").getAsString();

			String url = profile.getAsJsonObject("decodedTexture").getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
			loadSkin(entry, IO.read(url).asBytes());
		});
	}

	private static void tryLoadFromPlayerDB(PlayerListEntry entry) {
		Result.tryLoad(() -> {
			JsonObject data = IO.read("https://playerdb.co/api/player/minecraft/" + getNameOrUUID(entry))
				.asJsonObject();

			if (data.get("code").getAsString().equals("minecraft.invalid_username"))
				throw ERR_NOT_FOUND;

			if (!data.get("code").getAsString().equals("player.found"))
				throw ERR_INVALID;

			JsonObject profile = data.getAsJsonObject("data").getAsJsonObject("player");

			entry.id = profile.get("id").getAsString();
			entry.name = "!" + profile.get("username").getAsString().replace(' ', '+');

			String url = profile.get("skin_texture").getAsString();
			loadSkin(entry, IO.read(url).asBytes());
		});
	}

	private static void tryLoadFromLabyNet(PlayerListEntry entry) {
		Result.tryLoad(() -> {
			// Resolve UUID
			if (entry.id == null) {
				HttpGetOperation op = IO.read("https://laby.net/api/v3/user/" + entry.name + "/uniqueId");
				checkHttpCode(op);
				entry.id = op.asJsonObject().get("uuid").getAsString();
			}

			// Resolve name
			if (entry.name == null) {
				HttpGetOperation op = IO.read("https://laby.net/api/v3/user/" + entry.id + "/names");
				checkHttpCode(op);

				JsonArray names = op.asJsonArray();
				JsonObject name = names.get(names.size() - 1).getAsJsonObject();
				entry.name = name.get("name").getAsString();
			}

			// Resolve texture
			HttpGetOperation op = IO.read("https://laby.net/api/user/" + entry.id + "/get-textures");
			checkHttpCode(op);

			for (JsonElement skinElement : op.asJsonObject().getAsJsonArray("SKIN")) {
				JsonObject skin = skinElement.getAsJsonObject();
				if (!skin.has("active") || !skin.get("active").getAsBoolean())
					continue;

				String imageHash = skin.get("image_hash").getAsString();
				String url = "https://texture.laby.net/" + imageHash + ".png";
				loadSkin(entry, IO.read(url).asBytes());
				break;
			}
		});
	}

	private static Result loadFromAshcon(PlayerListEntry entry) {
		return Result.get(() -> {
			HttpGetOperation op = IO.read("https://api.ashcon.app/mojang/v2/user/" + getNameOrUUID(entry));
			checkHttpCode(op, 403); // Who TF uses 403 as not found
			JsonObject profile = op.asJsonObject();

			entry.name = profile.get("username").getAsString();
			entry.id = profile.get("uuid").getAsString();

			String skinData = profile.getAsJsonObject("textures").getAsJsonObject("skin").get("data").getAsString();
			loadSkin(entry, Base64.getDecoder().decode(skinData));

			return FOUND;
		});
	}

}
