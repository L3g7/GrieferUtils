package dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.token_providers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.core.Authorization;

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
