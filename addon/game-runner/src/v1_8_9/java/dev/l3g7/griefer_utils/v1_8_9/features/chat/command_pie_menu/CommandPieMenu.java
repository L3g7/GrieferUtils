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

package dev.l3g7.griefer_utils.v1_8_9.features.chat.command_pie_menu;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.config.Config;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.settings.types.KeySetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.settings.types.list.EntryAddSetting;
import dev.l3g7.griefer_utils.v1_8_9.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.v1_8_9.features.Feature;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

@Singleton
public class CommandPieMenu extends Feature {

	private String entryKey;

	private final PieMenu pieMenu = new PieMenu();

	private final SwitchSetting animation = SwitchSetting.create()
		.name("Animation")
		.description("Ob die Öffnen-Animation abgespielt werden soll.")
		.icon("command_pie_menu")
		.defaultValue(true);

	private final KeySetting key = KeySetting.create()
		.name("Taste")
		.icon("key")
		.description("Die Taste, mit der das Befehlsradialmenü geöffnet werden soll.")
		.pressCallback(p -> {
			if (mc().currentScreen != null || !isEnabled())
				return;

			if (p) {
				pieMenu.open(animation.get(), getMainElement());
				return;
			}

			pieMenu.close();
		});

	private final EntryAddSetting newEntrySetting = EntryAddSetting.create()
		.name("Seite hinzufügen")
		.callback(() -> {
			List<BaseSetting<?>> settings = getMainElement().getSubSettings();
			long pageNumber = settings.stream().filter(s -> s instanceof PieMenuSetting).count() + 1;
			PieMenuPageSetting setting = new PieMenuPageSetting("Seite " + pageNumber, new ArrayList<>());
			settings.add(settings.size() - 1, setting);
			setting.openSettings();
		});

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Befehlsradialmenü")
		.description("Ein Radialmenü zum schnellen Ausführen von Citybuild-bezogenen Befehlen.")
		.icon("command_pie_menu")
		.subSettings(key, animation, newEntrySetting);

	@Override
	public void init() {
		super.init();

		entryKey = getConfigKey() + ".pages";

		if (!Config.has(entryKey))
			return;

		JsonArray entries = Config.get(entryKey).getAsJsonArray();
		for (JsonElement entry : entries) {
			List<BaseSetting<?>> settings = enabled.getSubSettings();
			settings.add(settings.size() - 1, PieMenuPageSetting.fromJson(entry.getAsJsonObject()));
		}
	}

	public void save() {
		JsonArray array = new JsonArray();
		for (BaseSetting<?> page : enabled.getSubSettings())
			if (page instanceof PieMenuPageSetting)
				array.add(((PieMenuPageSetting) page).toJson());

		Config.set(entryKey, array);
		Config.save();
	}

	@EventListener
	private void onGuiOpen(GuiOpenEvent<?> event) {
		pieMenu.close();
	}

}
