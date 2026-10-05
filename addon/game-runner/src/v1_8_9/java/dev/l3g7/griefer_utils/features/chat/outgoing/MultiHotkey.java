/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.outgoing;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.CitybuildSetting;
import dev.l3g7.griefer_utils.core.settings.types.KeySetting;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.settings.types.list.ListEntry;
import dev.l3g7.griefer_utils.core.settings.types.list.ListSetting;
import dev.l3g7.griefer_utils.core.settings.types.list.StringListEntry;
import dev.l3g7.griefer_utils.features.Feature;

import java.util.Arrays;
import java.util.List;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

@Singleton
public class MultiHotkey extends Feature {

	private final ListSetting<HotkeySequence> entries = ListSetting.create(HotkeySequence.class)
		.name("Hotkeys")
		.icon("key")
		.unpacked();

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Multi-Hotkey")
		.description("Erlaubt das Ausführen von mehreren sequenziellen Befehlen auf Tastendruck.")
		.icon("key")
		.subSettings(entries);

	public static MultiHotkey get() {
		return get(MultiHotkey.class);
	}

	private static class HotkeySequence implements ListEntry<HotkeySequence> {

		public final StringSetting name = StringSetting.create()
			.name("Name")
			.description("Wie dieser Hotkey heißen soll.")
			.icon("name_tag");

		public final KeySetting key = KeySetting.create()
			.name("Taste")
			.description("Durch das Drücken welcher Taste/-n dieser Hotkey ausgelöst werden soll.")
			.icon("key")
			.pressCallback(b -> {if (b) trigger();});

		public final CitybuildSetting citybuild = CitybuildSetting.create()
			.name("Citybuild")
			.description("Auf welchem Citybuild dieser Hotkey funktionieren soll.");

		public final ListSetting<StringListEntry> commands = ListSetting.createStringList()
			.name("Befehle")
			.description("Zwischen welchen Befehlen gewechselt wird.")
			.icon("book_and_quill");

		private int triggers = 0;

		private void trigger() {
			if (!get().isEnabled())
				return;

			if (!this.citybuild.get().isOnCb())
				return;

			List<StringListEntry> values = this.commands.get();
			if (values.isEmpty())
				return;

			String command = values.get(triggers %= values.size()).get();
			if (!MessageEvent.MessageSendEvent.post(command))
				player().sendChatMessage(command);

			triggers++;
		}

		@Override
		public String getName() {
			return name.get();
		}

		@Override
		public String resourceIcon() {
			if (key.get().isEmpty() || commands.get().isEmpty())
				return "barrier";

			return "key";
		}

		private String formatSubtext(String format, String errorPrefix) {
			var keys = key.getFormattedKeys();
			if (keys == null)
				return errorPrefix + "Keine Taste hinterlegt.";

			var commands = this.commands.get();
			if (commands.isEmpty())
				return errorPrefix + "Keine Befehle hinterlegt.";

			return String.format(format, key.getFormattedKeys(), commands.size() + (commands.size() == 1 ? " Befehl" : " Befehle"));
		}

		@Override
		public String subtextLaby3() {
			return formatSubtext("§e[%s] §f§o➡ %s", "§7");
		}

		@Override
		public String subtextLaby4() {
			return formatSubtext("[%s] -> %s", "");
		}

		@Override
		public HotkeySequence createNew() {
			HotkeySequence hotkey = new HotkeySequence();
			hotkey.name.set("Neuer Hotkey");
			return hotkey;
		}

		@Override
		public List<BaseSetting<?>> toSettings() {
			return Arrays.asList(name, key, citybuild, commands);
		}

		@Override
		public void load(JsonElement data) {
			JsonObject page = data.getAsJsonObject();
			name.set(page.get("name").getAsString());
			key.set(key.getStorage().decodeFunc.apply(page.get("keys")));
			commands.set(commands.getStorage().decodeFunc.apply(page.get("commands")));
			citybuild.set(citybuild.getStorage().decodeFunc.apply(page.get("cb")));
		}

		@Override
		public JsonElement encode() {
			JsonObject obj = new JsonObject();
			obj.addProperty("name", name.get());
			obj.add("keys", key.getStorage().encodeFunc.apply(key.get()));
			obj.add("commands", commands.getStorage().encodeFunc.apply(commands.get()));
			obj.add("cb", citybuild.getStorage().encodeFunc.apply(citybuild.get()));
			return obj;
		}

	}
}
