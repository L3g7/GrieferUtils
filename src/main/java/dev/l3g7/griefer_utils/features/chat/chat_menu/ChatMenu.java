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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageModifyEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.ButtonSetting;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import dev.l3g7.griefer_utils.util.PlayerUtil;
import dev.l3g7.griefer_utils.util.misc.Config;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.core.LabyModCore;
import net.labymod.ingamechat.tabs.GuiChatNameHistory;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.l3g7.griefer_utils.features.chat.chat_menu.ChatMenuEntry.Action.OPEN_URL;
import static dev.l3g7.griefer_utils.features.chat.chat_menu.ChatMenuEntry.Action.RUN_CMD;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.labyMod;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static dev.l3g7.griefer_utils.util.misc.Constants.*;

@Singleton
public class ChatMenu extends Feature {

	public static final String COMMAND = "/grieferutils_click_event_replace_suggest_msg ";
	protected static final Map<ChatMenuEntry, String> DEFAULT_ENTRIES = new LinkedHashMap<ChatMenuEntry, String>() {{
		put(new ChatMenuEntry("Profil öffnen", RUN_CMD, "/profil %PLAYER%"), "open_profile");
		put(new ChatMenuEntry("Namensverlauf", ChatMenu::openNameHistory), "name_history");
		put(new ChatMenuEntry("Namen kopieren", ChatMenu::copyToClipboard), "copy_name");
		put(new ChatMenuEntry("Im Forum suchen", OPEN_URL, "https://forum.griefergames.de/search/?q=%PLAYER%"), "search_forum");
		put(new ChatMenuEntry("Inventar öffnen", RUN_CMD, "/invsee %PLAYER%"), "open_inv");
		put(new ChatMenuEntry("Ausrüstung ansehen", RUN_CMD, "/view %PLAYER%"), "view_gear");
		put(new ChatMenuEntry("EC öffnen", RUN_CMD, "/ec %PLAYER%"), "open_ec");

		for (ChatMenuEntry entry : keySet())
			entry.enableDefault();
	}};

	protected static final List<ChatMenuEntry> customEntries = new ArrayList<>();
	protected static ChatMenuRenderer renderer = null;
	protected static boolean loaded = false;

	protected static final ButtonSetting newEntrySetting = new ButtonSetting()
			.name("Neue Option erstellen")
			.callback(() -> {
				ChatMenuEntry newEntry = new ChatMenuEntry();
				((List<SettingsElement>) Reflection.get(mc().currentScreen, "path")).add(newEntry.getSetting());
				customEntries.add(newEntry);
				mc().currentScreen.initGui();
			});

	@MainElement(configureSubSettings = false)
	private static final BooleanSetting enabled = new BooleanSetting()
			.name("Chatmenü")
			.description("Öffnet ein Chatmenü beim Rechtsklicken auf einen Spieler im Chat.")
			.icon("chat_menu/chat");

	public ChatMenu() {
		loadEntries();
	}

	public static void saveEntries() {
		if (!loaded) // Don't save the config when starting
			return;

		for (ChatMenuEntry entry : DEFAULT_ENTRIES.keySet())
			Config.set("chat.chat_menu.entries." + DEFAULT_ENTRIES.get(entry), new JsonPrimitive(entry.isEnabled()));

		JsonArray array = new JsonArray();
		for (ChatMenuEntry customEntry : customEntries)
			if (customEntry.isValid())
				array.add(customEntry.toJson());

		Config.set("chat.chat_menu.entries.custom", array);
		Config.save();
	}

	protected static void updateSettings() {
		if (!loaded)
			return;

		List<SettingsElement> settings = new ArrayList<>();

		for (ChatMenuEntry entry : DEFAULT_ENTRIES.keySet())
			settings.add(entry.getSetting());

		for (ChatMenuEntry entry : customEntries) {
			if (!entry.isValid())
				continue;

			SettingsElement setting = entry.getSetting();
			setting.getSubSettings().add(new ButtonSetting()
					.name("Option entfernen")
					.callback(() -> {
						customEntries.remove(entry);
						settings.remove(setting);
						updateSettings();
						ArrayList<SettingsElement> list = Reflection.get(mc().currentScreen, "path");
						list.remove(list.size() - 1);
						mc().currentScreen.initGui();
					}));
			settings.add(setting);
		}

		settings.add(newEntrySetting);

		enabled.subSettings(settings.toArray(new SettingsElement[0]));
	}

	private void loadEntries() {

		for (ChatMenuEntry entry : DEFAULT_ENTRIES.keySet()) {
			String path = "chat.chat_menu.entries." + DEFAULT_ENTRIES.get(entry);

			if (Config.has(path))
				entry.setEnabled(Config.get(path).getAsBoolean());
		}

		String path = "chat.chat_menu.entries.custom";
		if (Config.has(path)) {
			for (JsonElement jsonElement : Config.get(path).getAsJsonArray()) {
				ChatMenuEntry entry = ChatMenuEntry.fromJson(jsonElement.getAsJsonObject());

				if (entry.isValid())
					customEntries.add(entry);
			}
		}

		loaded = true;
		updateSettings();
	}

	@EventListener
	public void onRender(TickEvent.RenderTickEvent event) {
		if (renderer != null)
			renderer.render();
	}

	// Add the /msg clickevent to plotchat- and private messages
	@EventListener
	public void onMsg(ClientChatReceivedEvent event) {
		String text = event.message.getFormattedText();

		for (Pattern p : new Pattern[] {PLOTCHAT_RECEIVE_PATTERN, MESSAGE_RECEIVE_PATTERN, MESSAGE_SEND_PATTERN}) {
			Matcher matcher = p.matcher(text);
			if (!matcher.find())
				continue;

			String name = matcher.group("name").replaceAll("§.", "");
			event.message.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/msg %s ", name)));
			return;
		}
	}

	@EventListener
	public void onMouse(GuiScreenEvent.MouseInputEvent.Pre event) {
		if (renderer != null && renderer.onMouse()) {
			renderer = null;
			return;
		}

		if (!Mouse.getEventButtonState())
			return;

		if (renderer != null && renderer.outOfBox())
			renderer = null;

		if (Mouse.getEventButton() != 1)
			return;

		String value = LabyModCore.getMinecraft().getClickEventValue(Mouse.getX(), Mouse.getY());
		if (value == null || !value.startsWith(COMMAND))
			return;

		List<ChatMenuEntry> entries = new ArrayList<>();
		DEFAULT_ENTRIES.keySet().stream().filter(ChatMenuEntry::isEnabled).forEach(entries::add);
		customEntries.stream().filter(e -> e.isEnabled() && e.isValid()).forEach(entries::add);

		renderer = new ChatMenuRenderer(entries, value.substring(COMMAND.length(), value.length() - 1));
		event.setCanceled(true);
	}

	@EventListener(priority = EventPriority.HIGHEST)
	public void modifyMessage(MessageModifyEvent event) {
		// Replaces all /msg click-events
		replaceMsgClickEvents(event.message);

		String name = null;

		for (Pattern p : MESSAGE_PATTERNS) {
			Matcher m = p.matcher(event.message.getFormattedText());

			if (m.find()) {
				name = m.group("name").replaceAll("§.", "");
				break;
			}
		}

		if (name == null)
			return;

		name = PlayerUtil.unnick(name);

		setClickEvent(event.message, COMMAND + name);
	}


	private static void replaceMsgClickEvents(IChatComponent component) {
		for (IChatComponent msg : component.getSiblings()) {
			ChatStyle style = msg.getChatStyle();
			ClickEvent event;

			if (style != null && (event = style.getChatClickEvent()) != null) {
				String value = event.getValue();

				if (value.startsWith("/msg "))
					setClickEvent(msg, COMMAND + value.substring(5));
			}
		}

	}

	private static void setClickEvent(IChatComponent msg, String command) {
		msg.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
	}

	@EventListener
	public void onKeyboard(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (renderer == null)
			return;

		if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == 1) {
			renderer = null;
			event.setCanceled(true);
		}

		mc().dispatchKeypresses();
	}

	@EventListener(triggerWhenDisabled = true)
	public void onMessageSend(MessageEvent.MessageSendEvent event) {
		if (event.message.startsWith(COMMAND)) {
			MinecraftUtil.suggest("/msg " + event.message.substring(COMMAND.length()));
			event.setCanceled(true);
		}
	}

	private static void openNameHistory(String name) {
		if (name.startsWith("!")) {
			labyMod().getGuiCustomAchievement().displayAchievement("§eUngültiger Name", "§fVon Bedrock-Spielern kann kein Namensverlauf abgefragt werden.");
			return;
		}

		mc().displayGuiScreen(new GuiChatNameHistory("", name));
	}

	private static void copyToClipboard(String text) {
		StringSelection selection = new StringSelection(text);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
		labyMod().getGuiCustomAchievement().displayAchievement("\"" + text + "\"",  "wurde in die Zwischenablage kopiert.");
	}

}