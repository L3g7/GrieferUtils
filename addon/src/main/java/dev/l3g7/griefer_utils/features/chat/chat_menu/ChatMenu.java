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

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.Config;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.event.events.MessageEvent.MessageModifyEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.NameCache;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.components.EntryAddSetting;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.core.LabyModCore;
import net.labymod.ingamechat.tabs.GuiChatNameHistory;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Consumer;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.core.misc.Constants.*;
import static dev.l3g7.griefer_utils.features.chat.chat_menu.ChatMenuEntry.Action.*;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.displayAchievement;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class ChatMenu extends Feature {

	private static final List<Pattern> PATTERNS = new ArrayList<Pattern>(MESSAGE_PATTERNS) {{add(STATUS_PATTERN);}};
	public static final String COMMAND = "/grieferutils_click_event_replace_suggest_msg ";
	protected static final List<ChatMenuEntry> DEFAULT_ENTRIES = ImmutableList.of(
		new ChatMenuEntry("Profil öffnen", RUN_CMD, "/profil %name%", "wooden_board"),
		new ChatMenuEntry("Namensverlauf", CONSUMER, (Consumer<String>) ChatMenu::openNameHistory, "yellow_name"),
		new ChatMenuEntry("Namen kopieren", CONSUMER, (Consumer<String>) ChatMenu::copyToClipboard, "yellow_name"),
		new ChatMenuEntry("Im Forum suchen", OPEN_URL, "https://forum.griefergames.de/search/?q=%name%", "earth_grid"),
		new ChatMenuEntry("Inventar öffnen", RUN_CMD, "/invsee %name%", "bundle"),
		new ChatMenuEntry("Ausrüstung ansehen", RUN_CMD, "/view %name%", Material.IRON_CHESTPLATE),
		new ChatMenuEntry("EC öffnen", RUN_CMD, "/ec %name%", "chest")
	);

	protected static ChatMenuRenderer renderer = null;

	protected static final EntryAddSetting newEntrySetting = new EntryAddSetting()
			.name("Neuen Menüpunkt erstellen")
		.callback(() -> Minecraft.getMinecraft().displayGuiScreen(new AddChatMenuEntryGui(null, Minecraft.getMinecraft().currentScreen)));

	@MainElement(configureSubSettings = false)
	private static final BooleanSetting enabled = new BooleanSetting()
			.name("Chatmenü")
			.description("Öffnet ein Chatmenü bei Rechtsklick auf einen Spieler im Chat.")
			.icon("player_menu");

	public ChatMenu() {
		loadEntries();
		List<SettingsElement> settings = new ArrayList<>();

		for (ChatMenuEntry entry : DEFAULT_ENTRIES) {
			settings.add(new BooleanSetting()
				.name(entry.name)
				.callback(v -> entry.enabled = v)
				.defaultValue(entry.enabled)
				.config("chat.chat_menu.entries." + entry.name)
				.icon(entry.icon));
		}

		settings.add(newEntrySetting);

		enabled.subSettings(settings.toArray(new SettingsElement[0]));


		String path = "chat.chat_menu.entries.custom";
		if (Config.has(path)) {
			for (JsonElement jsonElement : Config.get(path).getAsJsonArray()) {
				new EntryDisplaySetting(ChatMenuEntry.fromJson(jsonElement.getAsJsonObject()), enabled);
			}
		}

	}

	public static void saveEntries() {
		for (ChatMenuEntry entry : DEFAULT_ENTRIES)
			Config.set("chat.chat_menu.entries." + entry.name, new JsonPrimitive(entry.enabled));

		JsonArray array = new JsonArray();
		for (ChatMenuEntry customEntry : getCustom())
			if (customEntry.completed)
				array.add(customEntry.toJson());

		Config.set("chat.chat_menu.entries.custom", array);
		Config.save();
	}

	private void loadEntries() {

		for (ChatMenuEntry entry : DEFAULT_ENTRIES) {
			String path = "chat.chat_menu.entries." + entry.name;

			if (Config.has(path))
				entry.enabled = Config.get(path).getAsBoolean();
		}
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

		for (Pattern p : new Pattern[] {PLOTCHAT_RECEIVE_PATTERN, MESSAGE_RECEIVE_PATTERN, MESSAGE_SEND_PATTERN, STATUS_PATTERN}) {
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
		DEFAULT_ENTRIES.stream().filter(e -> e.enabled).forEach(entries::add);
		getCustom().stream().filter(e -> e.completed && e.enabled).forEach(entries::add);

		renderer = new ChatMenuRenderer(entries, value.substring(COMMAND.length(), value.length() - 1));
		event.setCanceled(true);
	}

	@EventListener(priority = EventPriority.HIGHEST)
	public void modifyMessage(MessageModifyEvent event) {
		// Replaces all /msg click-events
		replaceMsgClickEvents(event.message);

		String name = null;

		for (Pattern p : PATTERNS) {
			Matcher m = p.matcher(event.original.getFormattedText());

			if (m.find()) {
				name = m.group("name").replaceAll("§.", "");
				break;
			}
		}

		if (name == null)
			return;

		name = NameCache.ensureRealName(name);

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
			displayAchievement("§eUngültiger Name", "§fVon Bedrock-Spielern kann kein Namensverlauf abgefragt werden.");
			return;
		}

		mc().displayGuiScreen(new GuiChatNameHistory("", name));
	}

	private static void copyToClipboard(String text) {
		StringSelection selection = new StringSelection(text);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
		displayAchievement("\"" + text + "\"",  "wurde in die Zwischenablage kopiert.");
	}

	public static List<ChatMenuEntry> getCustom() {
		return enabled.getSubSettings().getElements()
			.stream()
			.filter(e -> e instanceof EntryDisplaySetting)
			.map(e -> ((EntryDisplaySetting) e).entry)
			.collect(Collectors.toList());
	}
}