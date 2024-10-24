/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.chat_menu.laby3;

import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.misc.gui.elements.SelectButtonGroup;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.util.ItemUtil;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.function.Consumer;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class ChatMenuEntry {

	String name = "";
	Action action;
	Object command = "";
	IconType iconType = null;
	Object icon = null;
	boolean completed = false;
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

	public JsonObject toJson() {
		JsonObject object = new JsonObject();

		object.addProperty("name", name);
		object.addProperty("action", action.name());
		object.addProperty("enabled", enabled);
		object.addProperty("command", (String) command);
		object.addProperty("icon_type", iconType.name());
		switch (iconType) {
			case ITEM:
				object.addProperty("icon", ItemUtil.serializeNBT(getIconAsItemStack()));
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
				entry.icon = ItemUtil.fromNBT(object.get("icon").getAsString());
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
				DrawUtils.bindTexture(new ResourceLocation("griefer_utils/user_content/" + icon.hashCode()));
				break;
			case ITEM:
				DrawUtils.drawItem(getIconAsItemStack(), x, y, null);
				return;
		}
		DrawUtils.drawTexture(x, y, 256.0, 256.0, w, h);
	}

	private ItemStack getIconAsItemStack() {
		return icon != null ? (ItemStack) icon : ItemUtil.MISSING_TEXTURE;
	}

	enum Action implements SelectButtonGroup.Selectable {
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

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getIcon() {
			return defaultIcon;
		}
	}

	enum IconType implements SelectButtonGroup.Selectable {
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

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getIcon() {
			return defaultIcon;
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

	public void trigger(String name, IChatComponent modifiedText, IChatComponent unmodifiedText) {
		switch (action) {
			case CONSUMER:
				((Consumer<String>) command).accept(name);
				break;
			case OPEN_URL:
				try {
					labyBridge.openWebsite(((String) command).replaceAll("(?i)%name%", name));
				} catch (RuntimeException e) {
					e.printStackTrace();
					LabyBridge.labyBridge.notifyError("Die URL, die geöffnet werden soll, ist ungültig.");
				}
				break;
			case RUN_CMD:
				String cmd = ((String) command).replaceAll("(?i)%name%", name);
				if (!MessageEvent.MessageSendEvent.post(cmd))
					MinecraftUtil.send((cmd));
				break;
			case SUGGEST_CMD:
				MinecraftUtil.suggest(((String) command).replaceAll("(?i)%name%", name));
				break;
		}
	}

}