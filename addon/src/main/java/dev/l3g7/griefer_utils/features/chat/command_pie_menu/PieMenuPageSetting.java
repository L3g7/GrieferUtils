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

package dev.l3g7.griefer_utils.features.chat.command_pie_menu;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.misc.gui.guis.AddonsGuiWithCustomBackButton;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import dev.l3g7.griefer_utils.settings.elements.components.EntryAddSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PieMenuPageSetting extends PieMenuSetting implements ElementBuilder<PieMenuPageSetting> {

	public final StringSetting name;

	private String defaultName;

	public PieMenuPageSetting(String name, ArrayList<SettingsElement> entrySettings) {
		this.name = new StringSetting()
			.name("Name")
			.callback(s -> name(s))
			.defaultValue(defaultName = name)
			.icon(Material.BOOK_AND_QUILL);

		icon(Material.EMPTY_MAP);

		entrySettings.forEach(e -> ((PieMenuSetting) e).container = this);
		entrySettings.add(0, this.name);
		entrySettings.add(new EntryAddSetting()
			.name("Eintrag hinzufügen")
			.callback(() -> {
				List<SettingsElement> settings = getSubSettings().getElements();
				PieMenuEntrySetting setting = new PieMenuEntrySetting("", "", null);
				setting.container = this;
				settings.add(settings.size() - 1, setting);
				setting.openSettings();
			}));

		subSettings(entrySettings.toArray(new SettingsElement[0]));
		setSettingEnabled(false);
		container = FileProvider.getSingleton(CommandPieMenu.class).getMainElement();
	}

	public void openSettings() {
		defaultName = name.get();
		mc.displayGuiScreen(new AddonsGuiWithCustomBackButton(() -> {
			if (!name.get().isEmpty()) {
				triggerOnChange();
				return;
			}

			if (defaultName.isEmpty()) {
				remove();
				return;
			}

			if (name.get().isEmpty())
				name.set(defaultName);
			triggerOnChange();
		}, this));
	}

	@Override
	protected void onChange() {
		triggerOnChange();
	}

	static void triggerOnChange() {
		FileProvider.getSingleton(CommandPieMenu.class).onChange();
	}

	public JsonObject toJson() {
		JsonObject pageObj = new JsonObject();
		pageObj.addProperty("name", name.get());

		JsonArray entries = new JsonArray();
		for (SettingsElement entry : getSubSettings().getElements()) {
			if (!(entry instanceof PieMenuEntrySetting))
				continue;

			PieMenuEntrySetting pieEntry = (PieMenuEntrySetting) entry;

			JsonObject entryObj = new JsonObject();
			entryObj.addProperty("name", pieEntry.name.get());
			entryObj.addProperty("command", pieEntry.command.get());
			entryObj.addProperty("cb", pieEntry.cityBuild.get().getDisplayName());

			entries.add(entryObj);
		}

		pageObj.add("entries", entries);
		return pageObj;
	}

	public static PieMenuPageSetting fromJson(JsonObject json) {
		ArrayList<SettingsElement> entrySettings = new ArrayList<>();

		for (JsonElement entry : json.getAsJsonArray("entries")) {
			JsonObject data = entry.getAsJsonObject();
			ItemStack stack = ItemUtil.CB_ITEMS.get(0);
			for (ItemStack cb : ItemUtil.CB_ITEMS) {
				if (cb.getDisplayName().equals(data.get("cb").getAsString())) {
					stack = cb;
					break;
				}
			}

			entrySettings.add(new PieMenuEntrySetting(
				data.get("name").getAsString(),
				data.get("command").getAsString(),
				stack
			));
		}

		return new PieMenuPageSetting(json.get("name").getAsString(), entrySettings);
	}

}
