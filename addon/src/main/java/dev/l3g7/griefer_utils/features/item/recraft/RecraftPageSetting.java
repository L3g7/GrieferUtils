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

package dev.l3g7.griefer_utils.features.item.recraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.features.item.recraft.RecraftRecording.RecordingDisplaySetting;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.misc.gui.guis.AddonsGuiWithCustomBackButton;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.ListEntrySetting;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import dev.l3g7.griefer_utils.settings.elements.components.EntryAddSetting;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.displayAchievement;
import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

class RecraftPageSetting extends ListEntrySetting {

	final StringSetting name;

	RecraftPageSetting(String name, List<SettingsElement> entrySettings) {
		super(true, true, true);

		icon(Material.EMPTY_MAP);
		entrySettings.forEach(e -> ((RecordingDisplaySetting) e).container = this);

		entrySettings.add(0, this.name = new StringSetting()
			.name("Name")
			.description("Wie diese Seite heißen soll.")
			.icon(Material.BOOK_AND_QUILL)
			.callback(title -> {
				if (title.trim().isEmpty())
					title = "Unbenannte Seite";

				HeaderSetting titleSetting = (HeaderSetting) getSubSettings().getElements().get(2);
				name(title);
				titleSetting.name("§e§l" + title);
			})
		);

		entrySettings.add(1, new HeaderSetting());

		entrySettings.add(new EntryAddSetting()
			.name("Aufzeichnung hinzufügen")
			.callback(() -> {
				if (!ServerCheck.isOnCitybuild()) {
					displayAchievement("§cAufzeichnungen", "§ckönnen nur auf einem Citybuild hinzugefügt werden.");
					return;
				}

				RecraftRecording recording = new RecraftRecording();
				List<SettingsElement> settings = getSubSettings().getElements();
				settings.add(settings.size() - 1, recording.mainSetting);

				recording.setTitle("Aufzeichnung hinzufügen");
				mc().displayGuiScreen(new AddonsGuiWithCustomBackButton(() -> {
					recording.setTitle(recording.mainSetting.getDisplayName());
					FileProvider.getSingleton(Recraft.class).save();
				}, recording.mainSetting));
			})
		);

		subSettings(entrySettings.toArray(new SettingsElement[0]));
		this.name.defaultValue(name);
		container = FileProvider.getSingleton(Recraft.class).getMainElement();
	}

	protected void onChange() {
		FileProvider.getSingleton(Recraft.class).save();
	}

	@Override
	protected void openSettings() {
		mc().displayGuiScreen(new AddonsGuiWithCustomBackButton(() -> FileProvider.getSingleton(Recraft.class).save(), this));
	}

	JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("name", name.get());

		List<RecordingDisplaySetting> recordings = Recraft.getSubSettingsOfType(this, RecordingDisplaySetting.class);

		JsonArray jsonRecordings = new JsonArray();
		for (RecordingDisplaySetting recording : recordings)
			jsonRecordings.add(recording.recording.toJson());
		object.add("recordings", jsonRecordings);

		return object;
	}

	static RecraftPageSetting fromJson(JsonObject object) {
		String name = object.get("name").getAsString();

		JsonArray recordings = object.getAsJsonArray("recordings");
		List<SettingsElement> settings = new ArrayList<>();
		for (JsonElement recording : recordings)
			settings.add(RecraftRecording.read(recording.getAsJsonObject()).mainSetting);

		return new RecraftPageSetting(name, settings);
	}

}
