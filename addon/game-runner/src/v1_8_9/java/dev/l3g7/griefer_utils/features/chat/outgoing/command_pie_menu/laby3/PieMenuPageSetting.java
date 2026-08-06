/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.outgoing.command_pie_menu.laby3;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.misc.griefer_games.Citybuild;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import dev.l3g7.griefer_utils.core.settings.types.list.EntryAddSetting;
import dev.l3g7.griefer_utils.labymod.laby3.util.AddonsGuiWithCustomBackButton;
import net.labymod.settings.elements.SettingsElement;

import java.util.ArrayList;
import java.util.List;

public class PieMenuPageSetting extends PieMenuSetting {

	public final StringSetting name;

	private String defaultName;

	public PieMenuPageSetting(String name, ArrayList<BaseSetting<?>> entrySettings) {
		this.name = StringSetting.create()
			.name("Name")
			.description("Wie diese Seite heißen soll.")
			.icon("name_tag")
			.callback(title -> {
				if (title.trim().isEmpty())
					title = "Unbenannte Seite";

				HeaderSetting titleSetting = (HeaderSetting) getSubSettings().getElements().get(2);
				name(title);
				titleSetting.name("§e§l" + title);
			});

		icon("command_suggestions");

		entrySettings.forEach(e -> ((PieMenuSetting) e).container = this);
		entrySettings.add(0, this.name);
		entrySettings.add(EntryAddSetting.create()
			.name("Eintrag hinzufügen")
			.callback(() -> {
				List<SettingsElement> settings = getSubSettings().getElements();
				PieMenuEntrySetting setting = new PieMenuEntrySetting("", "", Citybuild.ANY);
				setting.container = this;
				settings.add(settings.size() - 1, setting);
				setting.openSettings();
			}));

		subSettings(entrySettings.toArray(new BaseSetting[0]));
		this.name.set(defaultName = name);
		container = (SettingsElement) CommandPieMenu.get().getMainElement();
	}

	public void openSettings() {
		defaultName = name.get();
		mc.displayGuiScreen(new AddonsGuiWithCustomBackButton(() -> {
			if (!name.get().isEmpty()) {
				onChange();
				return;
			}

			if (defaultName.isEmpty()) {
				remove();
				return;
			}

			if (name.get().isEmpty())
				name.set(defaultName);
			onChange();
		}, this));
	}

	public JsonObject toJson() {
		JsonObject pageObj = new JsonObject();
		pageObj.addProperty("name", name.get());

		JsonArray entries = new JsonArray();
		for (SettingsElement entry : getSubSettings().getElements()) {
			if (!(entry instanceof PieMenuEntrySetting pieEntry))
				continue;

			JsonObject entryObj = new JsonObject();
			entryObj.addProperty("name", pieEntry.name.get());
			entryObj.addProperty("command", pieEntry.command.get());
			entryObj.add("cb", pieEntry.citybuild.getStorage().encodeFunc.apply(pieEntry.citybuild.get()));

			entries.add(entryObj);
		}

		pageObj.add("entries", entries);
		return pageObj;
	}

	public static PieMenuPageSetting fromJson(JsonObject json) {
		ArrayList<BaseSetting<?>> entrySettings = new ArrayList<>();

		for (JsonElement entry : json.getAsJsonArray("entries")) {
			JsonObject data = entry.getAsJsonObject();

			entrySettings.add(new PieMenuEntrySetting(
				data.get("name").getAsString(),
				data.get("command").getAsString(),
				Citybuild.parseSafe(data.get("cb").getAsString())
			));
		}

		return new PieMenuPageSetting(json.get("name").getAsString(), entrySettings);
	}

}
