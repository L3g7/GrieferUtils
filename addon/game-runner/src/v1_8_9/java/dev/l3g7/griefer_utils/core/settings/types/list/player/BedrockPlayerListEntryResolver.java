/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.settings.types.list.player;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.misc.xbox_profile_resolver.core.XboxProfile;
import dev.l3g7.griefer_utils.core.api.misc.xbox_profile_resolver.core.XboxProfileResolver;
import dev.l3g7.griefer_utils.core.api.util.io.HttpGetOperation;
import dev.l3g7.griefer_utils.core.api.util.io.IO;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import net.minecraft.client.renderer.texture.DynamicTexture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static dev.l3g7.griefer_utils.core.settings.types.list.player.Resolver.Result.FOUND;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

/**
 * Resolution strategy:
 * 1. Resolve using mc-api.io
 * 2. If this fails, verify existence using playerdb.co
 * 3. If the entry exists, resolve using official XBOX API
 * 4. If this fails, use partial results from playerdb.co
 */
class BedrockPlayerListEntryResolver extends Resolver {

	protected static Result load(PlayerListEntry entry) {
		return Result.get(() -> {
			tryLoadFromMcAPI(entry);

			Result partialRes = loadFromPlayerDB(entry);
			tryLoadFromXbox(entry);
			return partialRes;
		});
	}

	private static void tryLoadFromMcAPI(PlayerListEntry entry) {
		Result.tryLoad(() -> {
			HttpGetOperation op = entry.id == null
				? IO.read("https://mc-api.io/profile/" + entry.name.substring(1) + "/BEDROCK")
				: IO.read("https://mc-api.io/profile/" + entry.id);

			checkHttpCode(op);

			JsonObject profile = op.asJsonObject();
			entry.name = profile.get("name").getAsString();
			entry.id = profile.get("uuid").getAsString();

			String url = profile.getAsJsonObject("decodedTexture").getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
			loadSkin(entry, IO.read(url).asBytes());
		});
	}

	/**
	 * PlayerDB doesn't provide the skin, but since xbox has a low rate-limit, it is used to verify the existence.
	 */
	private static Result loadFromPlayerDB(PlayerListEntry entry) {
		return Result.get(() -> {
			JsonObject data = IO.read("https://playerdb.co/api/player/xbox/" + getNameOrUUID(entry))
				.asJsonObject();

			if (data.get("code").getAsString().equals("xbox.not_found"))
				throw ERR_NOT_FOUND;

			if (!data.get("code").getAsString().equals("player.found"))
				throw ERR_INVALID;

			JsonObject profile = data.getAsJsonObject("data").getAsJsonObject("player");

			entry.id = profile.get("id").getAsString();
			entry.name = "!" + profile.get("username").getAsString().replace(' ', '+');

			return FOUND;
		});
	}

	private static void tryLoadFromXbox(PlayerListEntry entry) throws IOException {
		if (!XboxProfileResolver.isAvailable())
			return;

		XboxProfile profile = entry.id == null
			? XboxProfileResolver.getProfileByGamerTag(entry.name.substring(1))
			: XboxProfileResolver.getProfileByXUID(entry.id);

		if (profile == null)
			throw ERR_NOT_FOUND;

		entry.id = profile.id;
		entry.name = "!" + profile.displayName.replace(' ', '+');

		String url = profile.avatar + "&height=128&width=128";
		loadSkin(entry, IO.read(url).asBytes());
		throw OK_FOUND;
	}

}
