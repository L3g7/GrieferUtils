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

package dev.l3g7.griefer_utils.features.item.recraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.config.Config;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.features.item.recraft.crafter.CraftPlayer;
import dev.l3g7.griefer_utils.features.item.recraft.recipe.RecipePlayer;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.misc.gui.guis.AddonsGuiWithCustomBackButton;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.settings.elements.KeySetting;
import dev.l3g7.griefer_utils.settings.elements.components.EntryAddSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

/**
 * Original version by Pleezon
 */
@Singleton
public class Recraft extends Feature {

	public static final RecraftRecording tempRecording = new RecraftRecording();
	public static boolean playingSuccessor;
	public static boolean ignoreSubIds;

	private final BooleanSetting ignoreSubIdsSetting = new BooleanSetting()
		.name("Sub-IDs ignorieren")
		.description("Ob beim Auswählen der Zutaten die Sub-IDs (z.B. unterschiedliche Holz-Typen) ignoriert werden sollen.")
		.icon(new ItemStack(Blocks.log, 1, 2))
		.callback(tempRecording.ignoreSubIds::set);

	private final KeySetting key = new KeySetting() {
		public int getObjectWidth() {
			return super.getObjectWidth() - 20;
		}
	}
		.name("Letzten Aufruf wiederholen")
		.description("Wiederholt den letzten \"/rezepte\" oder \"/craft\" Aufruf.")
		.icon(ItemUtil.createItem(Blocks.crafting_table, 0, true))
		.subSettings(ignoreSubIdsSetting)
		.settingsEnabled(true)
		.pressCallback(pressed -> {
			if (pressed && ServerCheck.isOnCitybuild() && isEnabled())
				tempRecording.play(false);
		});

	private final RecraftPieMenu pieMenu = new RecraftPieMenu();

	private final BooleanSetting animation = new BooleanSetting()
		.name("Animation")
		.description("Ob die Öffnen-Animation abgespielt werden soll.")
		.icon("command_pie_menu")
		.defaultValue(true);

	private final KeySetting openPieMenu = new KeySetting()
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
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Recraft")
		.description("Wiederholt \"/rezepte\" oder \"/craft\" Aufrufe oder dekomprimiert Items.\n\nVielen Dank an Pleezon/AntiBannSystem für die Hilfe beim AutoCrafter §c❤")
		.icon(ItemUtil.createItem(Blocks.crafting_table, 0, true))
		.subSettings(key, new HeaderSetting(), openPieMenu, animation, new HeaderSetting(), new EntryAddSetting()
			.name("Seite hinzufügen")
			.callback(() -> {
				List<SettingsElement> settings = getMainElement().getSubSettings().getElements();
				long pageNumber = settings.stream().filter(s -> s instanceof RecraftPageSetting).count() + 1;
				RecraftPageSetting setting = new RecraftPageSetting("Seite " + pageNumber, new ArrayList<>());
				settings.add(settings.size() - 1, setting);
				mc().displayGuiScreen(new AddonsGuiWithCustomBackButton(this::save, setting));
			}));

	@Override
	public void init() {
		super.init();

		if (!Config.has(getConfigKey() + ".pages"))
			return;

		JsonArray pages = Config.get(getConfigKey() + ".pages").getAsJsonArray();

		List<SettingsElement> settings = enabled.getSubSettings().getElements();
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

	public static boolean isPlaying() {
		return RecipePlayer.isPlaying() || CraftPlayer.isPlaying();
	}

	static <T> List<T> getSubSettingsOfType(SettingsElement container, Class<T> type) {
		List<T> subSettings = new ArrayList<>();

		for (SettingsElement element : container.getSubSettings().getElements())
			if (type.isInstance(element))
				subSettings.add(type.cast(element));

		return subSettings;
	}

	public static void iterate(BiConsumer<Integer, RecraftRecording> consumer) {
		List<RecraftPageSetting> pages = getSubSettingsOfType(FileProvider.getSingleton(Recraft.class).getMainElement(), RecraftPageSetting.class);
		for (int i = 0; i < pages.size(); i++) {
			List<RecraftRecording.RecordingDisplaySetting> recordings = Recraft.getSubSettingsOfType(pages.get(i), RecraftRecording.RecordingDisplaySetting.class);

			for (int j = 0; j < recordings.size(); j++) {
				RecraftRecording.RecordingDisplaySetting recording = recordings.get(j);
				consumer.accept(i << 16 | j, recording.recording);
			}
		}
	}

}
