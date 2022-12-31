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

package dev.l3g7.griefer_utils.features.chat.chat_reactor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.event.events.render.ChatLineAddEvent;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.settings.elements.ButtonSetting;
import dev.l3g7.griefer_utils.file_provider.Singleton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.util.misc.Config;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.settings.LabyModAddonsGui;
import net.labymod.settings.elements.SettingsElement;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class ChatReactor extends Feature {

	private static boolean loaded = false;
	private static final List<ChatReactorEntry> entries = new ArrayList<>();

	private static final ButtonSetting newEntrySetting = new ButtonSetting()
			.name("Neue Reaktion erstellen")
			.callback(() -> {
				ChatReactorEntry newEntry = new ChatReactorEntry();
				((List<SettingsElement>) Reflection.get(mc().currentScreen, "path")).add(newEntry.getSetting());
				entries.add(newEntry);
				mc().currentScreen.initGui();
			});

	@MainElement(configureSubSettings = false)
	private static final BooleanSetting enabled = new BooleanSetting()
		.name("ChatReactor")
		.description("Führt bei Chatnachrichten Befehle aus.")
		.icon("cpu")

	public ChatReactor() {
		loadEntries();
	}

	public static void saveEntries() {
		if (!loaded) // Don't save the config when starting
			return;

		JsonArray array = new JsonArray();
		for (ChatReactorEntry entry : entries)
			if (entry.isValid())
				array.add(entry.toJson());

		Config.set("chat.chat_reactor.entries", array);
		Config.save();
	}

	protected static void updateSettings() {
		if (!loaded)
			return;

		List<SettingsElement> settings = new ArrayList<>();

		for (ChatReactorEntry entry : entries) {
			if (!entry.isValid())
				continue;

			SettingsElement setting = entry.getSetting();
			setting.getSubSettings().add(new ButtonSetting()
					.name("Reaktion entfernen")
					.callback(() -> {
						entries.remove(entry);
						settings.remove(setting);
						saveEntries();
						updateSettings();
						((List<SettingsElement>) Reflection.get(mc().currentScreen, "path")).remove(((List<SettingsElement>) Reflection.get(mc().currentScreen, "path")).size() - 1);
						mc().currentScreen.initGui();
					}));
			settings.add(setting);
		}

		settings.add(newEntrySetting);
		enabled.subSettings(settings.toArray(new SettingsElement[0]));
	}

	private void loadEntries() {

		String path = "chat.chat_reactor.entries";
		if (Config.has(path)) {
			for (JsonElement jsonElement : Config.get(path).getAsJsonArray()) {
				ChatReactorEntry entry = ChatReactorEntry.fromJson(jsonElement.getAsJsonObject());

				if (!entry.isValid())
					continue;

				entries.add(entry);
			}
		}

		loaded = true;
		updateSettings();
	}

	@EventListener
	public void onMsg(ChatLineAddEvent event) {
		if (mc().currentScreen instanceof LabyModAddonsGui && ((List<SettingsElement>) Reflection.get(mc().currentScreen, "path")).contains(getMainElement()))
			return;

		for (ChatReactorEntry entry : entries)
			entry.checkMatch(event.getMessage());
	}
}