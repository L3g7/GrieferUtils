/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.api.misc.xbox_profile_resolver.token_providers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.l3g7.griefer_utils.api.misc.xbox_profile_resolver.core.Authorization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class LabyModTokenProvider implements TokenProvider {

	public boolean loadWithException() throws IOException {
		Path path = Paths.get("LabyMod", "accounts.json");
		if (!Files.exists(path))
			return false;

		JsonObject accounts = new JsonParser().parse(Files.newBufferedReader(path)).getAsJsonObject().getAsJsonObject("accounts");

		for (Map.Entry<String, JsonElement> entry : accounts.entrySet()) {

			Authorization.set(new Authorization(entry.getValue().getAsJsonObject().get("refresh_token").getAsString()));
			if (Authorization.get().validate())
				return true;
		}

		return false;
	}

}
