/*
 * This file is part of GrieferUtils https://github.com/L3g7/GrieferUtils.
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 the "License";
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
import dev.l3g7.griefer_utils.core.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.misc.config.Config;
import dev.l3g7.griefer_utils.event.EventListener;
import dev.l3g7.griefer_utils.features.Feature;
import dev.l3g7.griefer_utils.misc.ServerCheck;
import dev.l3g7.griefer_utils.settings.ElementBuilder.MainElement;
import dev.l3g7.griefer_utils.settings.elements.BooleanSetting;
import dev.l3g7.griefer_utils.settings.elements.KeySetting;
import dev.l3g7.griefer_utils.settings.elements.components.EntryAddSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;

import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

@Singleton
public class CommandPieMenu extends Feature {

	private String entryKey;

	private final PieMenu pieMenu = new PieMenu();
	private boolean isOpen = false;

	private final BooleanSetting animation = new BooleanSetting()
		.name("Animation")
		.description("Ob die Öffnen-Animation abgespielt werden soll")
		.icon("command_pie_menu")
		.defaultValue(true);

	private final KeySetting key = new KeySetting()
		.name("Taste")
		.icon("key")
		.pressCallback(p -> {
			if (mc().currentScreen != null || !isEnabled() || !ServerCheck.isOnGrieferGames())
				return;

			// Open
			if (p) {
				if (!isOpen) {
					pieMenu.open(animation.get(), getMainElement());
					isOpen = true;
				}
				return;
			}

			// Close
			if (isOpen) {
				pieMenu.close();
				isOpen = false;
			}
		});

	private final EntryAddSetting newEntrySetting = new EntryAddSetting()
		.name("Eintrag hinzufügen")
		.callback(() -> {
			List<SettingsElement> settings = getMainElement().getSubSettings().getElements();
			PieEntryDisplaySetting setting = new PieEntryDisplaySetting("", "", null);
			settings.add(settings.size() - 1, setting);
			setting.openSettings();
		});

	@MainElement
	private final BooleanSetting enabled = new BooleanSetting()
		.name("Befehlsradialmenü")
		.description("Ein Radialmenü zum schnellen Ausführen von Citybuild-bezogenen Befehlen.")
		.icon("command_pie_menu")
		.subSettings(key, animation, newEntrySetting);

	@Override
	public void init() {
		super.init();

		entryKey = getConfigKey() + ".entries";

		if (!Config.has(entryKey))
			return;

		JsonArray entries = Config.get(entryKey).getAsJsonArray();
		for (JsonElement entry : entries) {
			JsonObject data = entry.getAsJsonObject();

			ItemStack stack = ItemUtil.CB_ITEMS.get(0);
			for (ItemStack cb : ItemUtil.CB_ITEMS) {
				if (cb.getDisplayName().equals(data.get("cb").getAsString())) {
					stack = cb;
					break;
				}
			}

			PieEntryDisplaySetting pieEntry = new PieEntryDisplaySetting(
				data.get("name").getAsString(),
				data.get("command").getAsString(),
				stack
			);

			List<SettingsElement> settings = enabled.getSubSettings().getElements();
			settings.add(settings.size() - 1, pieEntry);
		}
	}

	public void onChange() {
		mc().currentScreen.initGui();

		JsonArray array = new JsonArray();
		for (SettingsElement element : enabled.getSubSettings().getElements()) {
			if (!(element instanceof PieEntryDisplaySetting))
				continue;

			PieEntryDisplaySetting pieEntry = (PieEntryDisplaySetting) element;

			JsonObject entry = new JsonObject();
			entry.addProperty("name", pieEntry.name.get());
			entry.addProperty("command", pieEntry.command.get());
			entry.addProperty("cb", pieEntry.cityBuild.get().getDisplayName());

			array.add(entry);
		}

		Config.set(entryKey, array);
		Config.save();
	}

	@EventListener
	private void onGuiOpen(GuiOpenEvent event) {
		if (isOpen) {
			pieMenu.close();
			isOpen = false;
		}
	}

}
