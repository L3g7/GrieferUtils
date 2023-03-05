package dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.token_providers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jna.platform.win32.Crypt32Util;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.core.Authorization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class MinecraftTokenProvider implements TokenProvider {

	public boolean loadWithException() throws IOException {
		Path path = Paths.get(System.getenv("AppData"), ".minecraft", "launcher_msa_credentials.bin");
		if (!Files.exists(path))
			return false;

		byte[] raw = Crypt32Util.cryptUnprotectData(Files.readAllBytes(path));
		JsonObject o = new JsonParser().parse(new InputStreamReader(new ByteArrayInputStream(raw))).getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : o.get("credentials").getAsJsonObject().entrySet()) {
			if (entry.getKey().equals("common"))
				continue;

			JsonObject credentials = entry.getValue().getAsJsonObject();
			JsonObject oauthToken = new JsonParser().parse(credentials.get("Xal.Production.Msa.Foci.1").getAsString()).getAsJsonObject();
			Authorization.set(new Authorization(oauthToken.get("refresh_token").getAsString()));

			if (Authorization.get().validate())
				return true;
		}

		return false;
	}

}
