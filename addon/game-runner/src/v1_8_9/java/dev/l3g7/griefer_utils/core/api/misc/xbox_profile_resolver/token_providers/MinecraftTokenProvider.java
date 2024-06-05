/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.xbox_profile_resolver.token_providers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import dev.l3g7.griefer_utils.core.api.misc.xbox_profile_resolver.core.Authorization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.api.bridges.MinecraftBridge.minecraftBridge;

public class MinecraftTokenProvider implements TokenProvider {

	public boolean loadWithException() throws IOException {
		Path path = Paths.get(System.getenv("AppData"), ".minecraft", "launcher_msa_credentials.bin");
		if (!Files.exists(path))
			return false;

		byte[] raw = minecraftBridge.winCryptUnprotectData(Files.readAllBytes(path));
		JsonObject o = Streams.parse(new JsonReader(new InputStreamReader(new ByteArrayInputStream(raw)))).getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : o.get("credentials").getAsJsonObject().entrySet()) {
			if (entry.getKey().equals("common"))
				continue;

			JsonObject credentials = entry.getValue().getAsJsonObject();
			JsonObject oauthToken = Streams.parse(new JsonReader(new StringReader(credentials.get("Xal.Production.Msa.Foci.1").getAsString()))).getAsJsonObject();
			Authorization.set(new Authorization(oauthToken.get("refresh_token").getAsString()));

			if (Authorization.get().validate())
				return true;
		}

		return false;
	}

}
