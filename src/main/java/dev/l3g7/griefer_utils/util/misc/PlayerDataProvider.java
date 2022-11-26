/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.util.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.l3g7.griefer_utils.util.IOUtil;
import dev.l3g7.griefer_utils.util.IOUtil.URLReadOperation;
import net.labymod.utils.JsonParse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

public class PlayerDataProvider {

	private static final List<PlayerData> playerData = new ArrayList<>();
	private static final PlayerData INVALID_NAME = new PlayerData();

	public static PlayerData get(String name) {
		if (!name.matches("^\\w{3,}$"))
			return INVALID_NAME;

		for (PlayerData data : playerData) {
			if (name.equalsIgnoreCase(data.name))
				return data;
		}
		PlayerData data = new PlayerData(name);
		playerData.add(data);
		return data;
	}

	public static PlayerData get(UUID uuid) {
		for (PlayerData data : playerData) {
			if (uuid.equals(data.uuid))
				return data;
		}
		PlayerData data = new PlayerData(uuid);
		playerData.add(data);
		return data;
	}

	public static class PlayerData {

		private String name;
		private UUID uuid;
		private ITextureObject skin = null;
		private boolean slim = false;
		private boolean invalid = false; // Whether the player name is invalid (the player doesn't exist)
		private boolean loaded = false; // Whether the player has been fully loaded

		private PlayerData() {
			name = null;
			invalid = true;
		}

		private PlayerData(String name) {
			this.name = name;
			load();
		}

		private PlayerData(UUID uuid) {
			this.uuid = uuid;
			load();
		}

		public String getName() {
			return name;
		}

		public UUID getUuid() {
			return uuid;
		}

		public ITextureObject getSkin() {
			return skin;
		}

		public boolean isSlim() {
			return slim;
		}

		public boolean isInvalid() {
			return invalid;
		}

		public boolean isLoaded() {
			return loaded;
		}

		private void load() {
			// Try to load it from tab list
			if (Minecraft.getMinecraft().getNetHandler() != null) {
				for (NetworkPlayerInfo info : Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap()) {
					if (info.getGameProfile().getName().equals(name) || info.getGameProfile().getId().equals(uuid)) {
						name = info.getGameProfile().getName();
						uuid = info.getGameProfile().getId();
						skin = mc().getTextureManager().getTexture(info.getLocationSkin());
						slim = info.getSkinType().equals("slim");
						System.out.println("foundInTab " + name + " " + uuid);
						return;
					}
				}
			}

			new Thread(() -> {
				try {
					System.out.println("loadFromMojang... " + uuid + " " + name);
					loadFromMojang();
				} catch (IOException e1) {
					try {
						System.out.println("loadFromAshcon... " + uuid + " " + name);
						loadFromAshcon();
					} catch (IOException e2) {
						System.out.println("failed... " + uuid + " " + name);
						e1.printStackTrace();
						e2.printStackTrace();
						invalid = true;
					}
				}
			}).start();
		}

		private void loadFromMojang() throws IOException {
			if (uuid == null) {
				URLReadOperation op = IOUtil.read("https://api.mojang.com/users/profiles/minecraft/" + name);
				// API returns 204 when an unknown user is requested.
				if (op.getResponseCode() == 204) {
					invalid = true;
					return;
				}

				if (op.getResponseCode() != 200) {
					// Probably rate-limited
					throw new IllegalStateException("Response code " + op.getResponseCode());
				}

				JsonObject data = op.asJsonObject().orElseThrow(() -> new JsonParseException("Invalid response for " + name));
				uuid = UUID.fromString(data.get("id").getAsString().replaceAll("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3-$4-$5"));
			}
			System.out.println("uuid resolved... " + uuid + " " + name);

			JsonObject profile = IOUtil.read("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid).asJsonObject().orElse(null);
			if (profile == null) {
				loadFromAshcon();
				return;
			}
			System.out.println("profile resolved... " + uuid + " " + profile);

			name = profile.get("name").getAsString();

			for (JsonElement element : profile.getAsJsonArray("properties")) {
				JsonObject property = element.getAsJsonObject();

				if (!property.get("name").getAsString().equals("textures"))
					continue;

				String url = JsonParse.parse(new String(Base64.getDecoder().decode(property.get("value").getAsString()))).getAsJsonObject().getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
				BufferedImage img = ImageIO.read(new URL(url));
				slim = img.getHeight() == 32;

				System.out.println("texture resolved... " + uuid);
				TickScheduler.runAfterRenderTicks(() -> {
					skin = new DynamicTexture(img);
					System.out.println("texture loaded... " + uuid);
					loaded = true;
					skin.loadTexture(mc().getResourceManager());
				}, 1);
			}
		}

		/**
		 * Ashcon's API doesn't have rate-limiting but is much slower, so Mojang's API is usually preferred.
		 */
		private void loadFromAshcon() throws IOException {
			JsonObject profile = IOUtil.read("https://api.ashcon.app/mojang/v2/user/" + (name == null ? uuid : name)).asJsonObject().orElseThrow(() -> new JsonParseException("Invalid response for " + name));
			System.out.println("a profile resolved... " + uuid + " " + name + " " + profile);
			name = profile.get("username").getAsString();
			uuid = UUID.fromString(profile.get("uuid").getAsString());
			slim = profile.get("textures").getAsJsonObject().get("slim").getAsBoolean();
			String skinData = profile.get("textures").getAsJsonObject().get("skin").getAsJsonObject().get("data").getAsString();
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(skinData)));
			System.out.println("a image read "+ uuid);

			TickScheduler.runAfterRenderTicks(() -> {
				skin = new DynamicTexture(img);
				System.out.println("a image loaded " + uuid);
				loaded = true;
				skin.loadTexture(mc().getResourceManager());
			}, 1);
		}

	}

}
