/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

package dev.l3g7.griefer_utils.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.authlib.properties.Property;
import dev.l3g7.griefer_utils.core.misc.TickScheduler;
import dev.l3g7.griefer_utils.core.util.IOUtil;
import dev.l3g7.griefer_utils.core.util.IOUtil.URLReadOperation;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.network.TabListEvent.TabListPlayerAddEvent;
import dev.l3g7.griefer_utils.util.PlayerUtil;
import net.labymod.utils.JsonParse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static java.lang.Thread.MIN_PRIORITY;
import static net.minecraftforge.fml.common.eventhandler.EventPriority.HIGHEST;

public class PlayerDataProvider {

	private static final List<PlayerData> playerData = new ArrayList<>();
	private static final PlayerData UNKNOWN_PLAYER = new PlayerData();

	public static PlayerData lookup(String name) {
		if (!PlayerUtil.isValid(name))
			return UNKNOWN_PLAYER;

		PlayerData data = get(name);
		if (data != UNKNOWN_PLAYER)
			return data;

		data = new PlayerData(name);
		playerData.add(data);
		return data;
	}

	public static PlayerData lookup(UUID uuid) {
		PlayerData data = get(uuid);
		if (data != UNKNOWN_PLAYER)
			return data;

		data = new PlayerData(uuid);
		playerData.add(data);
		return data;
	}

	public static PlayerData get(String name) {
		if (!PlayerUtil.isValid(name))
			return UNKNOWN_PLAYER;

		for (PlayerData data : playerData)
			if (name.equalsIgnoreCase(data.name))
				return data;

		return UNKNOWN_PLAYER;
	}

	public static PlayerData get(UUID uuid) {
		for (PlayerData data : playerData)
			if (uuid.equals(data.uuid))
				return data;

		return UNKNOWN_PLAYER;
	}

	@EventListener(priority = HIGHEST)
	private static void onJoin(TabListPlayerAddEvent event) {
		if (!PlayerUtil.isValid(event.data.getProfile().getName()))
			return;

		UUID uuid = event.data.getProfile().getId();
		if (get(uuid) != UNKNOWN_PLAYER)
			return;

		PlayerData data = new PlayerData();
		playerData.add(data);
		data.uuid = uuid;
		data.invalid = false;

		data.name = event.data.getProfile().getName();
		for (Map.Entry<String, Collection<Property>> entry : event.data.getProfile().getProperties().asMap().entrySet()) {
			for (Property property : entry.getValue()) {
				if (property.getName().equals("textures") && !property.getValue().isEmpty()) {
					String url = JsonParse.parse(new String(Base64.getDecoder().decode(property.getValue()))).getAsJsonObject().getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
					Thread loadTextureThread = new Thread(() -> {
						try {
							BufferedImage img = IOUtil.readImage(url);
							data.slim = img.getHeight() == 32;

							TickScheduler.runAfterRenderTicks(() -> {
								data.skin = new DynamicTexture(img);
								data.loaded = true;
								data.skin.loadTexture(mc().getResourceManager());
							}, 1);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}, "GrieferUtils Skin download for " + data.name);
					loadTextureThread.setPriority(MIN_PRIORITY);
					loadTextureThread.start();
				}
			}
		}
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
			uuid = null;
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
						return;
					}
				}
			}

			new Thread(() -> {
				try {
					loadFromMojang();
				} catch (IOException e1) {
					try {
						loadFromAshcon();
					} catch (IOException e2) {
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

			JsonObject profile = IOUtil.read("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid).asJsonObject().orElse(null);
			if (profile == null) {
				loadFromAshcon();
				return;
			}

			name = profile.get("name").getAsString();

			for (JsonElement element : profile.getAsJsonArray("properties")) {
				JsonObject property = element.getAsJsonObject();

				if (!property.get("name").getAsString().equals("textures"))
					continue;

				String url = JsonParse.parse(new String(Base64.getDecoder().decode(property.get("value").getAsString()))).getAsJsonObject().getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
				BufferedImage img = IOUtil.readImage(url);
				slim = img.getHeight() == 32;

				TickScheduler.runAfterRenderTicks(() -> {
					skin = new DynamicTexture(img);
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
			name = profile.get("username").getAsString();
			uuid = UUID.fromString(profile.get("uuid").getAsString());
			slim = profile.get("textures").getAsJsonObject().get("slim").getAsBoolean();
			String skinData = profile.get("textures").getAsJsonObject().get("skin").getAsJsonObject().get("data").getAsString();
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(skinData)));

			TickScheduler.runAfterRenderTicks(() -> {
				skin = new DynamicTexture(img);
				loaded = true;
				skin.loadTexture(mc().getResourceManager());
			}, 1);
		}

	}

}
