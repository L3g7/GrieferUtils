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

package dev.l3g7.griefer_utils.v1_8_9.features.item.recraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.laby4.settings.AbstractSettingImpl;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.StringSetting;
import dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.RecraftRecording.RecordingDisplaySetting;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.utils.Material;

import java.util.ArrayList;
import java.util.List;

class RecraftPageSetting extends AbstractSettingImpl<RecraftPageSetting, Void> { // extends ListEntrySetting

	final StringSetting name;

	RecraftPageSetting(String name, List<BaseSetting<?>> entrySettings) {
		super(null, null, null);
//		super(true, true, true);

		icon(Material.EMPTY_MAP);
//		entrySettings.forEach(e -> ((RecordingDisplaySetting) e).container = this);

		entrySettings.add(0, this.name = StringSetting.create()
			.name("Name")
			.description("Wie diese Seite heißen soll.")
			.icon(Material.BOOK_AND_QUILL)
			.callback(title -> {
				if (title.trim().isEmpty())
					title = "Unbenannte Seite";

				HeaderSetting titleSetting = (HeaderSetting) getSubSettings().get(2);
				name(title);
				titleSetting.name("§e§l" + title);
			})
		);

		entrySettings.add(1, HeaderSetting.create());

//		entrySettings.add(new EntryAddSetting()
//			.name("Aufzeichnung hinzufügen")
//			.callback(() -> {
//				if (!ServerCheck.isOnCitybuild()) {
//					displayAchievement("§cAufzeichnungen", "§ckönnen nur auf einem Citybuild hinzugefügt werden.");
//					return;
//				}
//
//				RecraftRecording recording = new RecraftRecording();
//				List<SettingsElement> settings = getSubSettings().getElements();
//				recording.mainSetting.container = this;
//				settings.add(settings.size() - 1, recording.mainSetting);
//
//				recording.setTitle("Aufzeichnung hinzufügen");
//				mc().displayGuiScreen(new AddonsGuiWithCustomBackButton(() -> {
//					recording.setTitle(recording.mainSetting.getDisplayName());
//					FileProvider.getSingleton(Recraft.class).save();
//				}, recording.mainSetting));
//			})
//		);

		subSettings(entrySettings.toArray(new BaseSetting[0]));
		this.name.defaultValue(name);
//		container = FileProvider.getSingleton(Recraft.class).getMainElement();
	}

	protected void onChange() {
//		if (!container.getSubSettings().getElements().contains(this))
//			for (RecordingDisplaySetting displaySetting : Recraft.getSubSettingsOfType(this, RecordingDisplaySetting.class))
//				displaySetting.recording.key.set(ImmutableSet.of());
//
//		FileProvider.getSingleton(Recraft.class).save();
	}

//	@Override
	protected void openSettings() {
//		mc().displayGuiScreen(new AddonsGuiWithCustomBackButton(() -> FileProvider.getSingleton(Recraft.class).save(), this));
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
		List<BaseSetting<?>> settings = new ArrayList<>();
		for (JsonElement recording : recordings)
			settings.add(RecraftRecording.read(recording.getAsJsonObject()).mainSetting);

		return new RecraftPageSetting(name, settings);
	}

	@Override
	protected Widget[] createWidgets() {
		return new Widget[0]; // TODO?
	}

}
