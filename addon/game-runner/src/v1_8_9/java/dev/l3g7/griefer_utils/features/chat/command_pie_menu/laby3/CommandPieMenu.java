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

package dev.l3g7.griefer_utils.features.chat.command_pie_menu.laby3;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.SwitchSettingImpl;
import dev.l3g7.griefer_utils.core.settings.types.KeySetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.bridges.laby3.temp.EntryAddSetting;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.GuiOpenEvent;
import net.labymod.settings.elements.SettingsElement;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;
import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

@Singleton
@ExclusiveTo(LABY_3)
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
				pieMenu.open(animation.get(), (SettingsElement) getMainElement());
				return;
			}

			pieMenu.close();
		});

	private final EntryAddSetting newEntrySetting = new EntryAddSetting()
		.name("Seite hinzufügen")
		.callback(() -> {
			List<SettingsElement> settings = ((SettingsElement) getMainElement()).getSubSettings().getElements();
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
			List<SettingsElement> settings = ((SwitchSettingImpl) enabled).getSubSettings().getElements();
			settings.add(settings.size() - 1, PieMenuPageSetting.fromJson(entry.getAsJsonObject()));
		}
	}

	public void save() {
		JsonArray array = new JsonArray();
		for (SettingsElement page : ((SwitchSettingImpl) enabled).getSubSettings().getElements())
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
