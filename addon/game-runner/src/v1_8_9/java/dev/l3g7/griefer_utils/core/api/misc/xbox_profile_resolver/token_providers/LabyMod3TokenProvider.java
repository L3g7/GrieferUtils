/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.xbox_profile_resolver.token_providers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.misc.xbox_profile_resolver.core.Authorization;
import dev.l3g7.griefer_utils.core.api.util.io.IO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class LabyMod3TokenProvider implements TokenProvider { // TODO: Support for Laby 4 accounts

	@Override
	public boolean load() {
		Path path = Paths.get("LabyMod", "accounts.json");
		if (!Files.exists(path))
			return false;

		JsonObject accounts = IO.read(path).asJsonObject().getAsJsonObject("accounts");

		for (Map.Entry<String, JsonElement> entry : accounts.entrySet()) {

			Authorization.set(new Authorization(entry.getValue().getAsJsonObject().get("refresh_token").getAsString()));
			if (Authorization.get().validate())
				return true;
		}

		return false;
	}

}
