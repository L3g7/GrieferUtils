/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.outgoing.multi_hotkey.laby3;

import com.google.common.collect.ImmutableSet;
import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.events.MessageEvent;
import dev.l3g7.griefer_utils.core.settings.types.*;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.KeySettingImpl;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.ListEntrySetting;
import dev.l3g7.griefer_utils.labymod.laby3.util.AddonsGuiWithCustomBackButton;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.SettingsElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static dev.l3g7.griefer_utils.core.api.bridges.LabyBridge.labyBridge;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.player;

public class HotkeyDisplaySetting extends ListEntrySetting {

	public final StringSetting name;
	public final KeySetting keys;
	public final StringListSetting commands;
	public final CitybuildSetting citybuild;

	private String defaultName;
	private Set<Integer> defaultKeys;
	private List<String> defaultCommands;
	private Citybuild defaultCitybuild;

	private int amountsTriggered = 0;

	public HotkeyDisplaySetting(String name, Set<Integer> keys, List<String> commands, Citybuild citybuild) {
		super(true, true, false);
		container = (SettingsElement) MultiHotkey.get().getMainElement();

		this.name = StringSetting.create()
			.name("Name")
			.description("Wie dieser Hotkey heißen soll.")
			.icon("name_tag")
			.callback(title -> {
				if (title.trim().isEmpty())
					title = "Unbenannter Hotkey";

				HeaderSetting titleSetting = (HeaderSetting) getSubSettings().getElements().get(2);
				titleSetting.name("§e§l" + title);
			});

		this.commands = StringListSetting.create()
			.name("Befehl hinzufügen")
			.set(defaultCommands = commands);

		this.citybuild = CitybuildSetting.create()
			.name("Citybuild")
			.description("Auf welchem Citybuild dieser Hotkey funktionieren soll.")
			.set((defaultCitybuild = citybuild) == null ? Citybuild.ANY : defaultCitybuild);

		this.keys = KeySetting.create()
			.name("Taste")
			.description("Durch das Drücken welcher Taste/-n dieser Hotkey ausgelöst werden soll.")
			.set(defaultKeys = keys)
			.icon("key")
			.pressCallback(b -> {
				if (!b || !MultiHotkey.get().isEnabled())
					return;

				if (!this.citybuild.get().isOnCb())
					return;

				if (this.commands.get().isEmpty()) {
					labyBridge.notify("§cFehler", "§cBitte füge den Eintrag neu hinzu.");
					return;
				}

				String command = this.commands.get().get(amountsTriggered %= this.commands.get().size());
				if (!MessageEvent.MessageSendEvent.post(command))
					player().sendChatMessage(command);

				amountsTriggered++;
			});


		subSettings(this.name, this.keys, this.commands, this.citybuild, HeaderSetting.create("§e§lBefehle").scale(0.7));
		this.name.set(defaultName = name);
		this.commands.create(this);
	}

	public void openSettings() {
		defaultName = name.get();
		defaultKeys = keys.get();
		defaultCommands = new ArrayList<>(commands.get());
		defaultCitybuild = citybuild.get();
		mc.displayGuiScreen(new AddonsGuiWithCustomBackButton(() -> {
			if (!name.get().isEmpty() && !keys.get().isEmpty() && !commands.get().isEmpty() && citybuild.get() != null) {
				onChange();
				return;
			}

			if (defaultName.isEmpty()) {
				remove();
				return;
			}

			if (name.get().isEmpty())
				name.set(defaultName);
			if (keys.get().isEmpty())
				keys.set(defaultKeys);
			if (commands.get().isEmpty())
				commands.set(defaultCommands);
			if (citybuild.get() == null)
				citybuild.set(defaultCitybuild);

			onChange();
		}, this));
	}

	protected void remove() {
		keys.set(ImmutableSet.of());
		super.remove();
	}

	protected void onChange() {
		icon(citybuild.get().toItemStack());
		MultiHotkey.get().onChange();
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);

		String subtitle = String.format("§e[%s] §f§o➡ %s", KeySettingImpl.formatKeys(keys.get()), commands.get().size() + (commands.get().size() == 1 ? " Befehl" : " Befehle"));

		String trimmedName = LabyMod.getInstance().getDrawUtils().trimStringToWidth(name.get(), maxX - x - 25 - 48);
		String trimmedSubtitle = LabyMod.getInstance().getDrawUtils().trimStringToWidth(subtitle, maxX - x - 25 - 48);
		LabyMod.getInstance().getDrawUtils().drawString(trimmedName + (trimmedName.equals(name.get()) ? "" : "…"), x + 25, y + 7 - 5);
		LabyMod.getInstance().getDrawUtils().drawString(trimmedSubtitle + (trimmedSubtitle.equals(subtitle) ? "" : "…"), x + 25, y + 7 + 5);
	}

}
