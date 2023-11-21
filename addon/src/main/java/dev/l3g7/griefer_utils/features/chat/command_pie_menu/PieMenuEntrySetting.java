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

import dev.l3g7.griefer_utils.misc.gui.guis.AddonsGuiWithCustomBackButton;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.elements.ItemSetting;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.utils.Material;
import net.minecraft.item.ItemStack;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;

public class PieMenuEntrySetting extends PieMenuSetting implements ElementBuilder<PieMenuEntrySetting> {

	public final StringSetting name;
	public final StringSetting command;
	public final ItemSetting citybuild;

	private String defaultName;
	private String defaultCommand;
	private ItemStack defaultCitybuild;

	public PieMenuEntrySetting(String name, String command, ItemStack citybuild) {
		this.name = new StringSetting()
			.name("Name")
			.description("Wie dieser Eintrag heißen soll.")
			.defaultValue(defaultName = name)
			.icon(Material.BOOK_AND_QUILL);

		this.command = new StringSetting()
			.name("Befehl")
			.description("Welcher Befehl ausgeführt werden soll, wenn dieser Eintrag ausgewählt wird.")
			.defaultValue(defaultCommand = command)
			.icon(Material.COMMAND);

		this.citybuild = new ItemSetting(ItemUtil.CB_ITEMS, false)
			.name("Citybuild")
			.description("Auf welchem Citybuild dieser Eintrag angezeigt werden soll.")
			.defaultValue((defaultCitybuild = citybuild) == null ? ItemUtil.CB_ITEMS.get(0) : defaultCitybuild);

		icon("command_pie_menu");
		subSettings(this.name, this.command, this.citybuild);
	}

	@Override
	protected void onChange() {
		PieMenuPageSetting.triggerOnChange();
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
	}

}
