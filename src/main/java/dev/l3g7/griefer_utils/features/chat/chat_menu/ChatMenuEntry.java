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

package dev.l3g7.griefer_utils.features.chat.chat_menu;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.settings.elements.*;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Consumer;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static dev.l3g7.griefer_utils.features.chat.chat_menu.ChatMenuEntry.Action.*;
import static dev.l3g7.griefer_utils.features.chat.chat_menu.ChatMenuEntry.IconType.*;
import static dev.l3g7.griefer_utils.features.chat.chat_menu.ItemSetting.MISSING_TEXTURE;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.labyMod;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static net.labymod.main.ModTextures.MISC_HEAD_QUESTION;

public class ChatMenuEntry {
	private static final Map<Action, String> descriptionMap = new HashMap<Action, String>() {{
		put(OPEN_URL, "Welche Webseite geöffnet werden soll.");
		put(RUN_CMD, "Welcher Befehl ausgeführt werden soll.");
		put(SUGGEST_CMD, "Welcher Befehl vorgeschlagen werden soll.");
	}};

	private static final Map<String, Object> DEFAULT_ICONS = new HashMap<String, Object>() {{
		put("open_profile", "wooden_board");
		put("name_history", "yellow_name");
		put("copy_name", "yellow_name");
		put("search_forum", "earth_grid");
		put("open_inv", "bundle");
		put("view_gear", Material.IRON_CHESTPLATE);
		put("open_ec", "chest");
	}};

	private final AtomicReference<String> icon = new AtomicReference<>(); // Atomic reference so it can be set in lambdas
	private final StringSetting name;
	private final StringSetting value;
	private final DropDownSetting<Action> action;
	private final DropDownSetting<IconType> iconType;
	private final SmallButtonSetting imageIcon;
	private final ItemSetting itemIcon;
	private Consumer<String> customConsumer = null;
	private IconType currentType;
	private boolean enabled = true;
	private boolean isDefault = false;

	public ChatMenuEntry() {
		this("", SUGGEST_CMD, "");
	}

	public ChatMenuEntry(String name, Action action, String value) {
		this(name, action, value, DEFAULT, null);
	}

	public ChatMenuEntry(String name, Action action, String value, IconType iconType, String icon) {
		currentType = iconType;

		this.name = new StringSetting()
				.name("Name")
				.description("Welcher Text im Chat-Menü angezeigt werden soll.")
				.defaultValue(name)
				.callback(v -> {
					ChatMenu.saveEntries();
					ChatMenu.updateSettings();
				})
				.icon(Material.NAME_TAG);

		this.value = new StringSetting()
				.name("Befehl")
				.description("Welcher Befehl im Chat-Menü angezeigt werden soll.")
				.defaultValue(value)
				.callback(v -> {
					ChatMenu.saveEntries();
					ChatMenu.updateSettings();
				})
				.icon(Material.PAPER);

		this.action = new DropDownSetting<>(Action.class)
				.name("Aktion")
				.description("Welche Aktion ausgeführt werden soll.")
				.icon(Material.COMMAND)
				.callback(a -> {
					this.value.name(a == OPEN_URL ? "URL" : "Befehl") // Update name and description of value
							.description(descriptionMap.get(a) + "\n(%PLAYER% wird durch den Spielernamen ersetzt.)");
					ChatMenu.saveEntries();
					ChatMenu.updateSettings();
				})
				.defaultValue(action)
				.stringProvider(Action::getName);

		this.imageIcon = new SmallButtonSetting()
			.name("Bild")
			.description("Welches Bild als Icon angezeigt werden soll")
			.icon(MISC_HEAD_QUESTION)
			.buttonIcon(new ControlElement.IconData("griefer_utils/icons/chat_menu/file.png"))
			.callback(() -> FileSelection.chooseFile(this::setIconFromFile));

		this.itemIcon = new ItemSetting()
			.name("Item")
			.description("Die ID / der Namespace des Items / Blocks, das als Icon angezeigt werden soll")
			.callback(stack -> {
				this.icon.set(stack.serializeNBT().toString());
				ChatMenu.saveEntries();
				ChatMenu.updateSettings();
			});

		this.iconType = new DropDownSetting<>(IconType.class)
			.name("Icon-Typ")
			.icon("chat_menu/chat");

		this.iconType.callback(t -> {
				ChatMenu.saveEntries();
				ChatMenu.updateSettings();

				if (!(mc().currentScreen instanceof LabyModAddonsGui) || currentType == t)
					return;

				List<SettingsElement> path = Reflection.get(mc().currentScreen, "path");
				List<SettingsElement> subSettings = path.get(path.size() - 1).getSubSettings().getElements();

				int index = subSettings.get(subSettings.size() - 1) instanceof ButtonSetting ? 1 : 0;

				if (currentType != DEFAULT)
					subSettings.remove(subSettings.size() - (index + 1));

				if (t != DEFAULT)
					subSettings.add(subSettings.size() - index, t == ITEM ? itemIcon : imageIcon);

				currentType = t;
				ChatMenu.saveEntries();
				ChatMenu.updateSettings();
				mc().currentScreen.initGui();
			})
			.defaultValue(iconType)
			.stringProvider(IconType::getName);

		if (iconType == DEFAULT)
			return;

		if (iconType == IMAGE) {
			setIconFromFile(new File(icon));
			return;
		}

		try {
			itemIcon.set(ItemStack.loadItemStackFromNBT(JsonToNBT.getTagFromJson(icon)));
		} catch (NBTException ignored) {}
	}

	public ChatMenuEntry(String name, Consumer<String> consumer) {
		this(name, SUGGEST_CMD, "", DEFAULT, "");
		customConsumer = consumer;
	}

	private void setIconFromFile(File file) {
		if (file == null)
			return;

		ResourceLocation location = new ResourceLocation("griefer_utils/user_content/" + file.hashCode());

		try {
			mc().getTextureManager().loadTexture(location, new DynamicTexture(ImageIO.read(file)));
		} catch (IOException | NullPointerException e) {
			labyMod().getGuiCustomAchievement().displayAchievement("§e§l§nFehlerhafte Datei", "§eDie Datei konnte nicht als Bild geladen werden.");
			return;
		}

		this.icon.set(file.getAbsolutePath());
		imageIcon.icon(location);

		ChatMenu.saveEntries();
		ChatMenu.updateSettings();
	}

	public ChatMenuEntry setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public void enableDefault() {
		isDefault = true;
		setEnabled(true);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isValid() {
		return !StringUtils.isBlank(name.get())
				&& action.get() != null
				&& !StringUtils.isBlank(value.get());
	}

	public String getName() {
		return name.get();
	}

	public Object getIcon() {
		if (iconType.get() != DEFAULT) {
			if (iconType.get() == IMAGE)
				return imageIcon.getIconData().getTextureIcon();

			ItemStack item = itemIcon.get();
			return item == null ? MISSING_TEXTURE : item;
		}

		if (isDefault) {
			Object icon = DEFAULT_ICONS.get(ChatMenu.DEFAULT_ENTRIES.get(this));
			if (icon instanceof Material)
				return icon;
			return new ResourceLocation("griefer_utils/icons/" + icon + ".png");
		}

		return action.get() == OPEN_URL ? "chat_menu/internet" : "chat_menu/chat";
	}

	public BooleanSetting getSetting() {
		BooleanSetting setting = new BooleanSetting()
				.callback(v -> {
					enabled = v;
					ChatMenu.saveEntries();
				})
				.defaultValue(enabled)
				.name(name.get())
				.icon(getIcon());

		if (isDefault || iconType == null /* Constructor hasn't finished */)
			return setting;

		List<SettingsElement> subSettings = new ArrayList<SettingsElement>(Arrays.asList(name, action, value, iconType));

		switch (iconType.get()) {
			case ITEM:
				subSettings.add(itemIcon);
				break;
			case IMAGE:
				subSettings.add(imageIcon);
		}

		return setting.subSettings(subSettings);
	}

	public Consumer<String> getConsumer() {
		if (customConsumer != null)
			return customConsumer;

		switch (action.get()) {
			case OPEN_URL:
				return s -> ChatMenuEntry.openWebsite(value.get().replaceAll("(?i)%PLAYER%", s));

			case RUN_CMD:
				return s -> MinecraftUtil.send(value.get().replaceAll("(?i)%PLAYER%", s));

			case SUGGEST_CMD:
				return s -> Minecraft.getMinecraft().displayGuiScreen(new GuiChat(value.get().replaceAll("(?i)%PLAYER%", s)));
		}

		return null;
	}

	public JsonObject toJson() {
		if (isDefault)
			return null;

		JsonObject object = new JsonObject();

		object.addProperty("name", name.get());
		object.addProperty("action", action.get().getConfigKey());
		object.addProperty("value", value.get());
		object.addProperty("icon_type", iconType.get().getConfigKey());
		object.addProperty("icon", icon.get());
		object.addProperty("enabled", enabled);

		return object;
	}

	public static ChatMenuEntry fromJson(JsonObject object) {
		return new ChatMenuEntry(
				object.get("name").getAsString(),
				Action.fromConfig(object.get("action").getAsString()),
				object.get("value").getAsString(),
				IconType.fromConfig(object.get("icon_type")),
				object.has("icon") ? object.get("icon").getAsString() : null
		).setEnabled(object.get("enabled").getAsBoolean());
	}

	public static void openWebsite(String url) {
		if (!mc().gameSettings.chatLinks)
			return;

		try {
			URI uri = new URI(url);
			String s = uri.getScheme();

			if (s == null)
				throw new URISyntaxException(url, "Missing protocol");

			if (!s.equalsIgnoreCase("http") && !s.equalsIgnoreCase("https"))
				throw new URISyntaxException(url, "Unsupported protocol: " + s.toLowerCase());

			Desktop.getDesktop().browse(uri);
		} catch (URISyntaxException use) {
			LogManager.getLogger().error("Can't open url " + url, use);
		} catch (IOException ioe) {
			LogManager.getLogger().error("Couldn't open link" , ioe);
		}
	}

	enum Action {
		OPEN_URL("Url öffnen", "open_url"),
		RUN_CMD("Befehl ausführen", "run"),
		SUGGEST_CMD("Befehl vorschlagen", "suggest");

		private final String name;
		private final String configKey;

		Action(String name, String configKey) {
			this.name = name;
			this.configKey = configKey;
		}

		private static Action fromConfig(String key) {
			for (Action value : Action.values())
				if (value.configKey.equals(key))
					return value;

			return SUGGEST_CMD;
		}

		private String getName() {
			return name;
		}

		private String getConfigKey() {
			return configKey;
		}
	}

	enum IconType {
		DEFAULT("Standard", "default"),
		ITEM("Item", "item"),
		IMAGE("Bild", "image");

		private final String name;
		private final String configKey;

		IconType(String name, String configKey) {
			this.name = name;
			this.configKey = configKey;
		}

		private static IconType fromConfig(JsonElement element) {
			if (element == null)
				return DEFAULT;

			for (IconType value : IconType.values())
				if (value.configKey.equals(element.getAsString()))
					return value;

			return DEFAULT;
		}

		private String getName() {
			return name;
		}

		private String getConfigKey() {
			return configKey;
		}
	}
}