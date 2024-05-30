/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.chat.multi_hotkey.laby3;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Citybuild;
import dev.l3g7.griefer_utils.api.misc.config.Config;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.laby3.settings.types.SwitchSettingImpl;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import net.labymod.settings.elements.SettingsElement;

import java.util.*;

import static dev.l3g7.griefer_utils.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.api.misc.Citybuild.CitybuildIconBridge.citybuildIconBridge;
import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;


@Singleton
@ExclusiveTo(LABY_3)
public class MultiHotkey extends Feature {

	private String entryKey;

	private final EntryAddSetting entryAddSetting = new EntryAddSetting()
		.name("Hotkey hinzufügen")
		.callback(() -> {
			List<SettingsElement> settings = ((SwitchSettingImpl) getMainElement()).getSubSettings().getElements();
			HotkeyDisplaySetting setting = new HotkeyDisplaySetting("", new HashSet<>(), new ArrayList<>(), null);
			setting.openSettings();
			settings.add(settings.size() - 1, setting);
		});

	@MainElement
	private final SwitchSettingImpl enabled = (SwitchSettingImpl) SwitchSetting.create()
		.name("Multi-Hotkey")
		.description("Erlaubt das Ausführen von mehreren sequenziellen Befehlen auf Tastendruck.")
		.icon("labymod_3/autotext")
		.subSettings(entryAddSetting);

	@Override
	public void init() {
		super.init();

		entryKey = getConfigKey() + ".entries";

		if (!Config.has(entryKey))
			return;

		JsonArray entries = Config.get(entryKey).getAsJsonArray();
		for (JsonElement entry : entries) {
			JsonObject data = entry.getAsJsonObject();

			Set<Integer> keys = new LinkedHashSet<>();
			data.get("keys").getAsJsonArray().forEach(e -> keys.add(e.getAsInt()));

			List<String> commands = new ArrayList<>();
			data.get("commands").getAsJsonArray().forEach(e -> commands.add(e.getAsString()));

			Citybuild cb = Citybuild.getCitybuild(data.get("cb").getAsString());

			HotkeyDisplaySetting hotKey = (HotkeyDisplaySetting) new HotkeyDisplaySetting(
				data.get("name").getAsString(),
				keys,
				commands,
				cb
			).icon(citybuildIconBridge.toItemStack(cb));

			List<SettingsElement> settings = enabled.getSubSettings().getElements();
			settings.add(settings.size() - 1, hotKey);
		}
	}

	public void onChange() {
		mc().currentScreen.initGui();

		JsonArray array = new JsonArray();
		for (SettingsElement element : enabled.getSubSettings().getElements()) {
			if (!(element instanceof HotkeyDisplaySetting))
				continue;

			HotkeyDisplaySetting hotkey = (HotkeyDisplaySetting) element;

			JsonObject entry = new JsonObject();
			entry.addProperty("name", hotkey.name.get());

			JsonArray keys = new JsonArray();
			hotkey.keys.get().forEach(key -> keys.add(new JsonPrimitive(key)));
			entry.add("keys", keys);

			JsonArray commands = new JsonArray();
			hotkey.commands.get().forEach(key -> commands.add(new JsonPrimitive(key)));
			entry.add("commands", commands);

			entry.addProperty("cb", hotkey.citybuild.get().getName());

			array.add(entry);
		}

		Config.set(entryKey, array);
		Config.save();
	}

}
