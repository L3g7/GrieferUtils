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

package dev.l3g7.griefer_utils.features.chat.chat_menu;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.Main;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.utils.Consumer;
import net.labymod.utils.Material;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

public class ChatMenuEntry {

	String name = "";
	Action action;
	Object command = "";
	IconType iconType = null;
	Object icon = null;
	boolean completed = false;
	int pos = -1;
	boolean enabled = true;

	public ChatMenuEntry() {}

	public ChatMenuEntry(String name, Action action, Object command, Object icon) {
		this.name = name;
		this.action = action;
		this.command = command;
		this.iconType = IconType.SYSTEM;
		this.icon = icon;
		this.completed = true;
	}

	@Override
	protected ChatMenuEntry clone() {
		ChatMenuEntry entry = new ChatMenuEntry(name, action, command, icon);
		entry.iconType = iconType;
		entry.command = command;
		entry.pos = pos;
		entry.enabled = enabled;
		return entry;
	}

	public JsonObject toJson() {
		JsonObject object = new JsonObject();

		object.addProperty("name", name);
		object.addProperty("action", action.name());
		object.addProperty("enabled", enabled);
		object.addProperty("command", (String) command);
		object.addProperty("icon_type", iconType.name());
		switch (iconType) {
			case ITEM:
				object.addProperty("icon", ((ItemStack) icon).serializeNBT().toString());
				break;
			case IMAGE_FILE:
				DynamicTexture t = (DynamicTexture) mc().getTextureManager().getTexture(new ResourceLocation("griefer_utils/user_content/" + icon.hashCode()));
				BufferedImage i = new BufferedImage(Reflection.get(t, "width"), Reflection.get(t, "height"), BufferedImage.TYPE_INT_ARGB);
				i.setRGB(0, 0, i.getWidth(), i.getHeight(), t.getTextureData(), 0, i.getWidth());
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				try {
					ImageIO.write(scale(i), "PNG", bytes);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				object.addProperty("icon", Base64.getEncoder().encodeToString(bytes.toByteArray()));
				object.addProperty("icon_name", ((File) icon).getName());
		}

		return object;
	}

	public static ChatMenuEntry fromJson(JsonObject object) {
		ChatMenuEntry entry = new ChatMenuEntry();
		entry.name = object.get("name").getAsString();
		entry.action = Action.valueOf(object.get("action").getAsString());
		entry.enabled = object.get("enabled").getAsBoolean();
		entry.command = object.get("command").getAsString();
		entry.iconType = IconType.valueOf(object.get("icon_type").getAsString());
		switch (entry.iconType) {
			case ITEM:
				try {
					entry.icon = ItemStack.loadItemStackFromNBT(JsonToNBT.getTagFromJson(object.get("icon").getAsString()));
				} catch (NBTException e) {
					throw new RuntimeException(e);
				}
				break;
			case IMAGE_FILE:
				entry.icon = new File(object.get("icon_name").getAsString());
				ResourceLocation location = new ResourceLocation("griefer_utils/user_content/" + entry.icon.hashCode());

				try {
					BufferedImage img = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(object.get("icon").getAsString())));
					mc().getTextureManager().loadTexture(location, new DynamicTexture(img));
				} catch (IOException | NullPointerException e) {
					throw new RuntimeException(e);
				}
		}
		entry.completed = true;

		return entry;
	}

	public void drawIcon(int x, int y, int w, int h) {
		switch (iconType) {
			case SYSTEM:
				mc().getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/" + icon + ".png"));
				break;
			case DEFAULT:
				mc().getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/" + action.defaultIcon + ".png"));
				break;
			case IMAGE_FILE:
				drawUtils().bindTexture(new ResourceLocation("griefer_utils/user_content/" + icon.hashCode()));
				break;
			case ITEM:
				drawUtils().drawItem(((ItemStack) icon), x, y, null);
				return;
		}
		drawUtils().drawTexture(x, y, 256.0, 256.0, w, h);
	}

	enum Action {
		CONSUMER(null, null),
		OPEN_URL("Url öffnen", "earth_grid"),
		RUN_CMD("Befehl ausführen", "cpu"),
		SUGGEST_CMD("Befehl vorschlagen", "speech_bubble");

		public final String name;
		public final String defaultIcon;
		Action(String name, String defaultIcon) {
			this.name = name;
			this.defaultIcon = defaultIcon;
		}
	}

	enum IconType {
		SYSTEM(null, null),
		DEFAULT("Standard", null),
		ITEM("Item", "gold_ingot"),
		IMAGE_FILE("Bild", "tree_file");

		public final String name;
		public final String defaultIcon;
		IconType(String name, String defaultIcon) {
			this.name = name;
			this.defaultIcon = defaultIcon;
		}

	}

	private BufferedImage scale(BufferedImage img) {
		if (img.getHeight() > 64 || img.getWidth() > 64) {
			float scaleFactor = (64f / (float) Math.max(img.getHeight(), img.getWidth()));
			Image scaledImg = (img.getScaledInstance((int) (img.getWidth() * scaleFactor), (int) (img.getHeight() * scaleFactor), Image.SCALE_DEFAULT));
			img = new BufferedImage(scaledImg.getWidth(null), scaledImg.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = img.createGraphics();
			g.drawImage(scaledImg, 0, 0, null);
			g.dispose();
			return img;
		}
		return img;
	}

	public void trigger(String name) {
		switch (action) {
			case CONSUMER:
				((Consumer<String>) command).accept(name);
				break;
			case OPEN_URL:
				try {
					Desktop.getDesktop().browse(new URI(((String) command).replaceAll("(?i)%name%", name)));
				} catch (IOException | URISyntaxException e) {
					throw new RuntimeException(e);
				}
				break;
			case RUN_CMD:
				MinecraftUtil.send(((String )command).replaceAll("(?i)%name%", name));
				break;
			case SUGGEST_CMD:
				MinecraftUtil.suggest(((String )command).replaceAll("(?i)%name%", name));
				break;
		}
	}
}