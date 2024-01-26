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
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.config.Config;
import dev.l3g7.griefer_utils.settings.AbstractSetting.MainElement;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.KeySetting;
import dev.l3g7.griefer_utils.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.settings.types.list.EntryAddSetting;
import dev.l3g7.griefer_utils.v1_8_9.features.Feature;
import dev.l3g7.griefer_utils.v1_8_9.misc.ServerCheck;
import dev.l3g7.griefer_utils.v1_8_9.util.ItemUtil;
import net.minecraft.init.Blocks;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil.mc;

/**
 * Original version by Pleezon
 */
@Singleton
public class Recraft extends Feature {

	static final RecraftRecording tempRecording = new RecraftRecording();

	private final KeySetting key = KeySetting.create()
		.name("Letzten Aufruf wiederholen")
		.description("Wiederholt den letzten \"/rezepte\" Aufruf.")
		.icon(ItemUtil.createItem(Blocks.crafting_table, 0, true))
		.pressCallback(pressed -> {
			if (pressed && ServerCheck.isOnCitybuild() && isEnabled())
				RecraftPlayer.play(tempRecording);
		});

	private final RecraftPieMenu pieMenu = new RecraftPieMenu();

	private final SwitchSetting animation = SwitchSetting.create()
		.name("Animation")
		.description("Ob die Öffnen-Animation abgespielt werden soll.")
		.icon("command_pie_menu")
		.defaultValue(true);

	private final KeySetting openPieMenu = KeySetting.create()
		.name("Radialmenü öffnen")
		.icon("key")
		.description("Die Taste, mit der das Radialmenü geöffnet werden soll.")
		.pressCallback(p -> {
			if (mc().currentScreen != null || !isEnabled())
				return;

			if (p) {
				pieMenu.open(animation.get(), getMainElement());
				return;
			}

			pieMenu.close();
		});

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Recraft")
		.description("Wiederholt \"/rezepte\" Aufrufe.")
		.icon(ItemUtil.createItem(Blocks.crafting_table, 0, true))
		.subSettings(key, HeaderSetting.create(), openPieMenu, animation, HeaderSetting.create(), EntryAddSetting.create()
			.name("Seite hinzufügen")
			.callback(() -> {
				List<BaseSetting<?>> settings = getMainElement().getSubSettings();
				long pageNumber = settings.stream().filter(s -> s instanceof RecraftPageSetting).count() + 1;
				RecraftPageSetting setting = new RecraftPageSetting("Seite " + pageNumber, new ArrayList<>());
				settings.add(settings.size() - 1, setting);
				// TODO: mc().displayGuiScreen(new AddonsGuiWithCustomBackButton(this::save, setting));
			}));

	@Override
	public void init() {
		super.init();

		if (!Config.has(getConfigKey() + ".pages"))
			return;

		JsonArray pages = Config.get(getConfigKey() + ".pages").getAsJsonArray();

		List<BaseSetting<?>> settings = enabled.getSubSettings();
		for (JsonElement page : pages)
			settings.add(settings.size() - 1, RecraftPageSetting.fromJson(page.getAsJsonObject()));
	}

	void save() {
		JsonArray jsonPages = new JsonArray();

		List<RecraftPageSetting> pages = getSubSettingsOfType(enabled, RecraftPageSetting.class);

		for (RecraftPageSetting page : pages)
			jsonPages.add(page.toJson());

		Config.set(getConfigKey() + ".pages", jsonPages);
		Config.save();
	}

	static <T> List<T> getSubSettingsOfType(BaseSetting<?> container, Class<T> type) {
		List<T> subSettings = new ArrayList<>();

		for (BaseSetting<?> element : container.getSubSettings())
			if (type.isInstance(element))
				subSettings.add(type.cast(element));

		return subSettings;
	}

}
