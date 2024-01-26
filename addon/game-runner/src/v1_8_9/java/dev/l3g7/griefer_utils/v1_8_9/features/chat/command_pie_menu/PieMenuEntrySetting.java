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

import dev.l3g7.griefer_utils.api.misc.Citybuild;
import dev.l3g7.griefer_utils.settings.types.CitybuildSetting;
import dev.l3g7.griefer_utils.settings.types.HeaderSetting;
import dev.l3g7.griefer_utils.settings.types.StringSetting;
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

				HeaderSetting titleSetting = (HeaderSetting) getSubSettings().get(2);
				titleSetting.name("§e§l" + title);
			});

		this.command = StringSetting.create()
			.name("Befehl")
			.description("Welcher Befehl ausgeführt werden soll, wenn dieser Eintrag ausgewählt wird.")
			.defaultValue(defaultCommand = command)
			.icon(Blocks.command_block);

		defaultCitybuild = citybuild;
		this.citybuild = CitybuildSetting.create()
			.name("Citybuild")
			.description("Auf welchem Citybuild dieser Eintrag angezeigt werden soll.")
			.defaultValue(citybuild);

		icon("command_pie_menu");
		subSettings(this.name, this.command, this.citybuild);
		this.name.defaultValue(defaultName = name);
	}

	public void openSettings() {}

		/*
		TODO:
	public void openSettings() {
		defaultName = name.get();
		defaultCommand = command.get();
		defaultCitybuild = c(citybuild.get());
		mc().displayGuiScreen(new AddonsGuiWithCustomBackButton(() -> {
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
			if (citybuild.get() == null)
				citybuild.set(defaultCitybuild);
			onChange();
		}, this));
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);

		String cb = "§e[" + MinecraftUtil.getCitybuildAbbreviation(citybuild.get().getDisplayName()) + "] ";
		int cbWidth = drawUtils().getStringWidth(cb);

		String trimmedName = drawUtils().trimStringToWidth(name.get(), maxX - x - 25 - 48);
		String trimmedCommand = drawUtils().trimStringToWidth(command.get(), maxX - x - 25 - 48 - drawUtils().getStringWidth("➡ ") - cbWidth);
		drawUtils().drawString(trimmedName + (trimmedName.equals(name.get()) ? "" : "…"), x + 25, y + 7 - 5);
		drawUtils().drawString("§o➡ " + trimmedCommand + (trimmedCommand.equals(command.get()) ? "" : "…"), x + 25 + cbWidth, y + 7 + 5);
		drawUtils().drawString(cb, x + 25, y + 7 + 5);
	}*/

}
