/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.player_resolver.xbox.token_providers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.jna.platform.win32.Crypt32Util;
import dev.l3g7.griefer_utils.core.misc.player_resolver.xbox.Authorization;
import dev.l3g7.griefer_utils.core.api.util.io.IO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class MinecraftTokenProvider implements TokenProvider {

	@Override
	public boolean load() {
		Path path = Paths.get(System.getenv("AppData"), ".minecraft", "launcher_msa_credentials.bin");
		if (!Files.exists(path))
			return false;

		byte[] raw = Crypt32Util.cryptUnprotectData(IO.read(path).asBytes());
		JsonObject o = IO.read(raw).asJsonObject();
		for (Map.Entry<String, JsonElement> entry : o.get("credentials").getAsJsonObject().entrySet()) {
			if (entry.getKey().equals("common"))
				continue;

			JsonObject credentials = entry.getValue().getAsJsonObject();
			JsonObject oauthToken = IO.read(credentials.get("Xal.Production.Msa.Foci.1").getAsString().getBytes()).asJsonObject();
			Authorization.set(new Authorization(oauthToken.get("refresh_token").getAsString()));

			if (Authorization.get().validate())
				return true;
		}

		return false;
	}

}
