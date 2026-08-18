/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.misc.gui.elements.SelectButtonGroup;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.settings.types.list.ListEntry;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class ChatMenuEntry implements ListEntry<ChatMenuEntry> {

	public String name;
	public Action action;
	public Object command;
	public IconType iconType;
	public Object icon;
	public boolean enabled = true;

	public ChatMenuEntry() {
		this("Neuer Eintrag", Action.CONSUMER, "", IconType.DEFAULT, null);
	}

	public ChatMenuEntry(String name, Action action, Object command, IconType iconType, Object icon) {
		this.name = name;
		this.action = action;
		this.command = command;
		this.iconType = iconType;
		this.icon = icon;
	}

	@Override
	public ChatMenuEntry createNew() {
		return new ChatMenuEntry();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String subtext() {
		return String.valueOf(command);
	}

	@Override
	public String resourceIcon() {
		return switch (iconType) {
			case SYSTEM -> String.valueOf(icon);
			case DEFAULT -> action.defaultIcon;
			case IMAGE_FILE -> "user_content/" + icon.hashCode();
			case ITEM -> null;
		};
	}

	@Override
	public ItemStack itemIcon() {
		if (iconType != IconType.ITEM)
			return null;

		return getIconAsItemStack();
	}

	public ItemStack getIconAsItemStack() {
		return icon != null ? (ItemStack) icon : ItemUtil.MISSING_TEXTURE;
	}

	public void drawIcon(int x, int y, int w, int h) {
		switch (iconType) {
			case SYSTEM ->
				mc().getTextureManager().bindTexture(new ResourceLocation("griefer_utils", "icons/" + icon + ".png"));
			case DEFAULT ->
				mc().getTextureManager().bindTexture(new ResourceLocation("griefer_utils", "icons/" + action.defaultIcon + ".png"));
			case IMAGE_FILE ->
				DrawUtils.bindTexture(new ResourceLocation("griefer_utils", "icons/user_content/" + icon.hashCode() + ".png"));
			case ITEM -> {
				DrawUtils.drawItem(getIconAsItemStack(), x, y, null);
				return;
			}
		}
		DrawUtils.drawTexture(x, y, 256.0, 256.0, w, h);
	}

	@Override
	public void load(JsonElement element) {
		JsonObject data = element.getAsJsonObject();

		this.name = data.get("name").getAsString();
		this.action = Action.valueOf(data.get("action").getAsString());
		this.enabled = data.get("enabled").getAsBoolean();
		this.command = data.get("command").getAsString();
		this.iconType = IconType.valueOf(data.get("icon_type").getAsString());

		switch (this.iconType) {
			case ITEM -> this.icon = ItemUtil.fromNBT(data.get("icon").getAsString());
			case IMAGE_FILE -> {
				this.icon = new File(data.get("icon_name").getAsString());
				ResourceLocation location = new ResourceLocation("griefer_utils", "icons/user_content/" + this.icon.hashCode() + ".png");
				try {
					BufferedImage img = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(data.get("icon").getAsString())));
					mc().getTextureManager().loadTexture(location, new DynamicTexture(img));
				} catch (IOException | NullPointerException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	public JsonElement encode() {
		JsonObject object = new JsonObject();

		object.addProperty("name", name);
		object.addProperty("action", action.name());
		object.addProperty("enabled", enabled);
		object.addProperty("command", (String) command);
		object.addProperty("icon_type", iconType.name());

		switch (iconType) {
			case ITEM -> object.addProperty("icon", ItemUtil.serializeNBT(getIconAsItemStack()));
			case IMAGE_FILE -> {
				DynamicTexture t = (DynamicTexture) mc().getTextureManager().getTexture(new ResourceLocation("griefer_utils", "icons/user_content/" + icon.hashCode() + ".png"));
				BufferedImage i = new BufferedImage(Reflection.get(t, "width"), Reflection.get(t, "height"), BufferedImage.TYPE_INT_ARGB);
				i.setRGB(0, 0, i.getWidth(), i.getHeight(), t.getTextureData(), 0, i.getWidth());

				// Scale to 64x64
				if (i.getHeight() > 64 || i.getWidth() > 64) {
					float scaleFactor = (64f / (float) Math.max(i.getHeight(), i.getWidth()));
					Image scaledImg = (i.getScaledInstance((int) (i.getWidth() * scaleFactor), (int) (i.getHeight() * scaleFactor), Image.SCALE_DEFAULT));
					i = new BufferedImage(scaledImg.getWidth(null), scaledImg.getHeight(null), BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = i.createGraphics();
					g.drawImage(scaledImg, 0, 0, null);
					g.dispose();
				}

				try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
					ImageIO.write(i, "PNG", bytes);
					object.addProperty("icon", Base64.getEncoder().encodeToString(bytes.toByteArray()));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				object.addProperty("icon_name", ((File) icon).getName());
			}
		}

		return object;
	}

	public void trigger(String name, IChatComponent modifiedComponent, IChatComponent originalComponent) {
		this.action.trigger.accept(command, name);
	}

	public enum Action implements SelectButtonGroup.Selectable {
		CONSUMER(null, null, (command, name) ->
			Reflection.<Consumer<String>>c(command).accept(name)),

		OPEN_URL("Url öffnen", "earth_grid", (command, name) -> {
			try {
				labyBridge.openWebsite(((String) command).replaceAll("(?i)%name%", name));
			} catch (RuntimeException e) {
				LabyBridge.labyBridge.notifyError("Die URL, die geöffnet werden soll, ist ungültig.");
			}
		}),

		RUN_CMD("Befehl ausführen", "cpu", (command, name) -> {
			String cmd = ((String) command).replaceAll("(?i)%name%", name);
			if (!MessageEvent.MessageSendEvent.post(cmd))
				MinecraftUtil.send((cmd));
		}),

		SUGGEST_CMD("Befehl vorschlagen", "chat", (command, name) ->
			MinecraftUtil.suggest(((String) command).replaceAll("(?i)%name%", name)));

		public final String name;
		public final String defaultIcon;
		private final BiConsumer<Object, String> trigger;

		Action(String name, String defaultIcon, BiConsumer<Object, String> trigger) {
			this.name = name;
			this.defaultIcon = defaultIcon;
			this.trigger = trigger;
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

	public enum IconType implements SelectButtonGroup.Selectable {
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

}