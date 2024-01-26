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

package dev.l3g7.griefer_utils.v1_8_9.features.chat.chat_menu;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.config.Config;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.settings.types.list.EntryAddSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.GuiScreenEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.TickEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.Feature;
import dev.l3g7.griefer_utils.v1_8_9.misc.NameCache;
import dev.l3g7.griefer_utils.v1_8_9.util.ChatLineUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.l3g7.griefer_utils.api.misc.Constants.*;
import static dev.l3g7.griefer_utils.v1_8_9.features.chat.chat_menu.ChatMenuEntry.Action.*;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

@Singleton
public class ChatMenu extends Feature {

	protected static final CopyTextEntry COPY_TEXT_ENTRY = new CopyTextEntry();

	protected static final List<ChatMenuEntry> DEFAULT_ENTRIES = ImmutableList.of(
		new ChatMenuEntry("Profil öffnen", RUN_CMD, "/profil %name%", "wooden_board"),
		new ChatMenuEntry("Namensverlauf", CONSUMER, (Consumer<String>) ChatMenu::openNameHistory, "yellow_name"),
		new ChatMenuEntry("Namen kopieren", CONSUMER, (Consumer<String>) ChatMenu::copyToClipboard, "yellow_name"),
		new ChatMenuEntry("Im Forum suchen", OPEN_URL, "https://forum.griefergames.de/search/?q=%name%", "earth_grid"),
		new ChatMenuEntry("Inventar öffnen", RUN_CMD, "/invsee %name%", "bundle"),
		new ChatMenuEntry("Ausrüstung ansehen", RUN_CMD, "/view %name%", new ItemStack(Items.iron_chestplate)),
		new ChatMenuEntry("EC öffnen", RUN_CMD, "/ec %name%", "chest")
	);

	protected static ChatMenuRenderer renderer = null;

	protected static final EntryAddSetting newEntrySetting = EntryAddSetting.create()
			.name("Neuen Menüpunkt erstellen")
		.callback(() -> Minecraft.getMinecraft().displayGuiScreen(new AddChatMenuEntryGui(null, Minecraft.getMinecraft().currentScreen)));

	@MainElement(configureSubSettings = false)
	private static final SwitchSetting enabled = SwitchSetting.create()
			.name("Chatmenü")
			.description("Öffnet ein Chatmenü bei Rechtsklick auf einen Spieler im Chat.")
			.icon("player_menu");

	public ChatMenu() {
		loadEntries();
		List<BaseSetting> settings = new ArrayList<>();

		for (ChatMenuEntry entry : DEFAULT_ENTRIES) {
			settings.add(SwitchSetting.create()
				.name(entry.name)
				.callback(v -> entry.enabled = v)
				.defaultValue(entry.enabled)
				.config("chat.chat_menu.entries." + entry.name)
				.icon(entry.icon));
		}

		settings.add(COPY_TEXT_ENTRY.getSetting());
		settings.add(newEntrySetting);

		enabled.subSettings(settings.toArray(new BaseSetting[0]));


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

	@EventListener
	public void onMouse(GuiScreenEvent.MouseInputEvent.Pre event) {
		if (renderer != null && renderer.onMouse()) {
			renderer = null;
			event.cancel();
			return;
		}

		if (!Mouse.getEventButtonState())
			return;

		if (renderer != null && renderer.outOfBox())
			renderer = null;

		if (Mouse.getEventButton() != 1 || !(mc().currentScreen instanceof GuiChat))
			return;

		IChatComponent icc = ChatLineUtil.getUnmodifiedIChatComponent(ChatLineUtil.getHoveredComponent());
		if (icc == null) // Didn't click on a line
			return;

		String name = null;

		for (Pattern p : new Pattern[] {GLOBAL_RECEIVE_PATTERN, PLOTCHAT_RECEIVE_PATTERN, MESSAGE_RECEIVE_PATTERN, MESSAGE_SEND_PATTERN, STATUS_PATTERN, GLOBAL_CHAT_PATTERN}) {
			Matcher matcher = p.matcher(icc.getFormattedText());
			if (!matcher.find())
				continue;

			name = matcher.group("name").replaceAll("§.", "");
			break;
		}

		if (name == null)
			return;

		List<ChatMenuEntry> entries = new ArrayList<>();
		DEFAULT_ENTRIES.forEach(e -> { if (e.enabled) entries.add(e); });
		if (COPY_TEXT_ENTRY.enabled) entries.add(COPY_TEXT_ENTRY);
		getCustom().forEach(e -> { if (e.enabled) entries.add(e); });

		name = name.replaceAll("§.", "").trim();
		String realName = NameCache.ensureRealName(name);
		if (realName == null)
			realName = name;

		renderer = new ChatMenuRenderer(entries, realName, ChatLineUtil.getHoveredComponent());
		event.cancel();
	}

	@EventListener
	public void onKeyboard(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (renderer == null)
			return;

		if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == 1) {
			renderer = null;
			event.cancel();
		}

		mc().dispatchKeypresses();
	}

	private static void openNameHistory(String name) {
		if (name.startsWith("!")) {
			LabyBridge.labyBridge.notifyMildError("Von Bedrock-Spielern kann kein Namensverlauf abgefragt werden.");
			return;
		}

		// TODO: mc().displayGuiScreen(new GuiChatNameHistory("", name));
	}


	static void copyToClipboard(String text) {
		StringSelection selection = new StringSelection(text);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
		LabyBridge.labyBridge.notify("\"" + text + "\"",  "wurde in die Zwischenablage kopiert.");
	}

	public static List<ChatMenuEntry> getCustom() {
		return enabled.getSubSettings()
			.stream()
			.filter(e -> e instanceof EntryDisplaySetting)
			.map(e -> ((EntryDisplaySetting) e).entry)
			.collect(Collectors.toList());
	}

	/*
	// TODO: @Mixin(value = MinecraftImplementation.class, remap = false)
	private static class MixinMinecraftImplementation {

		@Inject(method = "getClickEventValue", at = @At("HEAD"), cancellable = true)
		public void injectGetClickEventValue(int x, int y, CallbackInfoReturnable<String> cir) {
			if (FileProvider.getSingleton(ChatMenu.class).isEnabled())
				cir.setReturnValue(null);
		}

	}*/

}