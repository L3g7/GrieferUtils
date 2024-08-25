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

import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.bridges.laby3.temp.AddonsGuiWithCustomBackButton;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.settings.types.CitybuildSetting;
import dev.l3g7.griefer_utils.core.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

public class PieMenuEntrySetting extends PieMenuSetting {

	public final StringSetting name;
	public final StringSetting command;
	public final CitybuildSetting citybuild;

	private String defaultName;
	private String defaultCommand;
	private Citybuild defaultCitybuild;

	public PieMenuEntrySetting(String name, String command, Citybuild citybuild) {
		this.name = StringSetting.create()
			.name("Name")
			.description("Wie dieser Eintrag heißen soll.")
			.icon(Items.writable_book)
			.callback(title -> {
				if (title.trim().isEmpty())
					title = "Unbenannter Eintrag";

				HeaderSetting titleSetting = (HeaderSetting) getSubSettings().getElements().get(2);
				titleSetting.name("§e§l" + title);
			});

		this.command = StringSetting.create()
			.name("Befehl")
			.description("Welcher Befehl ausgeführt werden soll, wenn dieser Eintrag ausgewählt wird.")
			.defaultValue(defaultCommand = command)
			.icon(Blocks.command_block);

		this.citybuild = CitybuildSetting.create()
			.name("Citybuild")
			.description("Auf welchem Citybuild dieser Eintrag angezeigt werden soll.")
			.defaultValue(defaultCitybuild = citybuild);

		icon("command_pie_menu");
		subSettings(this.name, this.command, this.citybuild);
		this.name.defaultValue(defaultName = name);
	}

	public void openSettings() {
		defaultName = name.get();
		defaultCommand = command.get();
		defaultCitybuild = citybuild.get();
		mc.displayGuiScreen(new AddonsGuiWithCustomBackButton(() -> {
			if (!name.get().isEmpty() && !command.get().isEmpty() && citybuild.get() != null) {
				onChange();
				return;
			}

			if (defaultName.isEmpty()) {
				remove();
				return;
			}

			if (name.get().isEmpty())
				name.set(defaultName);
			if (command.get().isEmpty())
				command.set(defaultCommand);
			if (citybuild.get() == null) {
				citybuild.set(defaultCitybuild);
			}

			onChange();
		}, this));
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);

		String cb = "§e[" + MinecraftUtil.getCitybuildAbbreviation(citybuild.get().getName()) + "] ";
		int cbWidth = DrawUtils.getStringWidth(cb);

		String trimmedName = DrawUtils.trimStringToWidth(name.get(), maxX - x - 25 - 48);
		String trimmedCommand = DrawUtils.trimStringToWidth(command.get(), maxX - x - 25 - 48 - DrawUtils.getStringWidth("➡ ") - cbWidth);
		DrawUtils.drawString(trimmedName + (trimmedName.equals(name.get()) ? "" : "…"), x + 25, y + 7 - 5);
		DrawUtils.drawString("§o➡ " + trimmedCommand + (trimmedCommand.equals(command.get()) ? "" : "…"), x + 25 + cbWidth, y + 7 + 5);
		DrawUtils.drawString(cb, x + 25, y + 7 + 5);
	}

}
