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

package dev.l3g7.griefer_utils.v1_8_9.settings.player_list;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import dev.l3g7.griefer_utils.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.api.misc.CustomSSLSocketFactoryProvider;
import dev.l3g7.griefer_utils.api.misc.xbox_profile_resolver.core.XboxProfile;
import dev.l3g7.griefer_utils.api.misc.xbox_profile_resolver.core.XboxProfileResolver;
import dev.l3g7.griefer_utils.api.util.IOUtil;
import dev.l3g7.griefer_utils.v1_8_9.misc.TickScheduler;
import net.minecraft.client.renderer.texture.DynamicTexture;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

public class PlayerListEntryResolver {

	protected static final Map<String, PlayerListEntry> LOOKUP_MAP = new HashMap<>();

	/**
	 * PlayerDB doesn't provide the skin, but since xbox has a low rate-limit, it is used to verify the existence.
	 */
	public static void loadFromPlayerDB(PlayerListEntry entry) {
		IOUtil.URLReadOperation op = IOUtil.read("https://playerdb.co/api/player/xbox/" + (entry.id == null ? entry.name.substring(1) : entry.id));

		if (op.getResponseCode() != 200) {
			entry.exists = false;
			LOOKUP_MAP.put(entry.name, entry);
			return;
		}

		JsonObject data = op.asJsonObject().orElseThrow(() -> new JsonParseException("Invalid response for " + entry.name));
		if (!data.get("code").getAsString().equals("player.found"))
			throw new RuntimeException("Invalid response for " + entry.name);

		JsonObject playerData = data.getAsJsonObject("data").getAsJsonObject("player");

		entry.id = playerData.get("id").getAsString();
		entry.name = "!" + playerData.get("username").getAsString().replace(' ', '+');
		entry.loaded = true;
	}

	public static void loadFromXbox(PlayerListEntry entry) throws IOException {
		if (!XboxProfileResolver.isAvailable())
			return;

		XboxProfile profile = entry.name == null ? XboxProfileResolver.getProfileByXUID(entry.id) : XboxProfileResolver.getProfileByGamerTag(entry.name.substring(1));
		if (profile == null) {
			entry.exists = false;
			LOOKUP_MAP.put(entry.name, entry);
			return;
		}

		entry.id = profile.id;
		entry.name = "!" + profile.displayName.replace(' ', '+');
		entry.loaded = true;

		try {
			BufferedImage img = readImage(profile.avatar + "&height=128&width=128");

			TickScheduler.runAfterRenderTicks(() -> {
				entry.skin = new DynamicTexture(img);
				LOOKUP_MAP.put(entry.name, entry);
				entry.skin.loadTexture(mc().getResourceManager());
			}, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadFromMojang(PlayerListEntry entry) throws IOException {
		if (entry.id == null) {
			IOUtil.URLReadOperation op = IOUtil.read("https://api.mojang.com/users/profiles/minecraft/" + entry.name);
			// API returns 404 or 204 when an unknown user is requested.
			if (op.getResponseCode() == 404 || op.getResponseCode() == 204) {
				entry.exists = false;
				LOOKUP_MAP.put(entry.name, entry);
				return;
			}

			if (op.getResponseCode() != 200) {
				// Probably rate-limited
				throw new IllegalStateException("Response code " + op.getResponseCode());
			}

			JsonObject data = op.asJsonObject().orElseThrow(() -> new JsonParseException("Invalid response for " + entry.name));
			entry.id = data.get("id").getAsString().replaceAll("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3-$4-$5");
		}
		JsonObject profile = IOUtil.read("https://sessionserver.mojang.com/session/minecraft/profile/" + entry.id).asJsonObject().orElse(null);
		if (profile == null) {
			loadFromAshcon(entry);
			return;
		}

		entry.loaded = true;
		entry.name = profile.get("name").getAsString();

		for (JsonElement element : profile.getAsJsonArray("properties")) {
			JsonObject property = element.getAsJsonObject();

			if (!property.get("name").getAsString().equals("textures"))
				continue;

			String url = JsonParser.parseString(new String(Base64.getDecoder().decode(property.get("value").getAsString()))).getAsJsonObject().getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
			BufferedImage img = readImage(url);
			entry.oldSkin = img.getHeight() == 32;

			TickScheduler.runAfterRenderTicks(() -> {
				entry.skin = new DynamicTexture(img);
				LOOKUP_MAP.put(entry.name, entry);
				entry.skin.loadTexture(mc().getResourceManager());
			}, 1);
		}
	}

	/**
	 * Ashcon's API doesn't have rate-limiting but is much slower, so Mojang's API is usually preferred.
	 */
	public static void loadFromAshcon(PlayerListEntry entry) throws IOException {
		JsonObject profile = IOUtil.read("https://api.ashcon.app/mojang/v2/user/" + (entry.name == null ? entry.id : entry.name)).asJsonObject().orElseThrow(() -> new JsonParseException("Invalid response for " + entry.name));
		entry.name = profile.get("username").getAsString();
		entry.id = profile.get("uuid").getAsString();
		entry.loaded = true;
		entry.oldSkin = profile.getAsJsonObject("textures").get("slim").getAsBoolean();
		String skinData = profile.getAsJsonObject("textures").getAsJsonObject("skin").get("data").getAsString();
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(skinData)));

		TickScheduler.runAfterRenderTicks(() -> {
			entry.skin = new DynamicTexture(img);
			entry.skin.loadTexture(mc().getResourceManager());
		}, 1);
	}


	private static BufferedImage readImage(String url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

		if (conn instanceof HttpsURLConnection)
			((HttpsURLConnection) conn).setSSLSocketFactory(CustomSSLSocketFactoryProvider.getCustomFactory());

		conn.addRequestProperty("User-Agent", "GrieferUtils v" + LabyBridge.labyBridge.addonVersion() + " | github.com/L3g7/GrieferUtils");
		conn.setConnectTimeout(10000);
		return ImageIO.read(conn.getInputStream());
	}

}
