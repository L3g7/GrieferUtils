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

import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.misc.gui.AddonsGuiWithCustomBackButton;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.elements.ItemSetting;
import dev.l3g7.griefer_utils.settings.elements.StringSetting;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.Material;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;

public class PieEntryDisplaySetting extends ControlElement implements ElementBuilder<PieEntryDisplaySetting> {

	public final StringSetting name;
	public final StringSetting command;
	public final ItemSetting cityBuild;

	private String defaultName;
	private String defaultCommand;
	private ItemStack defaultCityBuild;

	private final IconStorage iconStorage = new IconStorage();
	private boolean hoveringDelete = false;
	private boolean hoveringEdit = false;

	public PieEntryDisplaySetting(String name, String command, ItemStack cityBuild) {
		super("§f", null);

		this.name = new StringSetting()
			.name("Name")
			.defaultValue(defaultName = name)
			.icon(Material.BOOK_AND_QUILL);

		this.command = new StringSetting()
			.name("Befehl")
			.defaultValue(defaultCommand = command)
			.icon(Material.COMMAND);

		this.cityBuild = new ItemSetting(ItemUtil.CB_ITEMS, false)
			.name("CityBuild")
			.defaultValue((defaultCityBuild = cityBuild) == null ? ItemUtil.CB_ITEMS.get(0) : defaultCityBuild);

		icon("command_pie_menu");

		subSettings(this.name, this.command, this.cityBuild);
		setSettingEnabled(false);
	}

	@Override
	public IconStorage getIconStorage() {
		return iconStorage;
	}

	public void openSettings() {
		defaultName = name.get();
		defaultCommand = command.get();
		defaultCityBuild = cityBuild.get();
		mc.displayGuiScreen(new AddonsGuiWithCustomBackButton(() -> {
			if (!name.get().isEmpty() && !command.get().isEmpty() && cityBuild.get() != null) {
				triggerOnChange();
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
			if (cityBuild.get() == null)
				cityBuild.set(defaultCityBuild);
			triggerOnChange();
		}, this));
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (hoveringEdit) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
			openSettings();
			return;
		}

		if (!hoveringDelete)
			return;

		mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
		remove();
	}

	private void remove() {
		FileProvider.getSingleton(CommandPieMenu.class).getMainElement().getSubSettings().getElements().remove(this);
		triggerOnChange();
	}

	private void triggerOnChange() {
		FileProvider.getSingleton(CommandPieMenu.class).onChange();
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		hideSubListButton();
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		drawIcon(x, y);

		mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;
		String cb = "§e[" + MinecraftUtil.getCityBuildAbbreviation(cityBuild.get().getDisplayName()) + "] ";
		int cbWidth = drawUtils().getStringWidth(cb);

		String trimmedName = drawUtils().trimStringToWidth(name.get(), maxX - x - 25 - 48);
		String trimmedCommand = drawUtils().trimStringToWidth(command.get(), maxX - x - 25 - 48 - drawUtils().getStringWidth("➡ ") - cbWidth);
		drawUtils().drawString(trimmedName + (trimmedName.equals(name.get()) ? "" : "…"), x + 25, y + 7 - 5);
		drawUtils().drawString("§o➡ " + trimmedCommand + (trimmedCommand.equals(command.get()) ? "" : "…"), x + 25 + cbWidth, y + 7 + 5);
		drawUtils().drawString(cb, x + 25, y + 7 + 5);

		int xPosition = maxX - 20;
		double yPosition = y + 4.5;

		hoveringDelete = mouseX >= xPosition && mouseY >= yPosition && mouseX <= xPosition + 15.5 && mouseY <= yPosition + 16;
		xPosition -= 20;
		hoveringEdit = mouseX >= xPosition && mouseY >= yPosition && mouseX <= xPosition + 15.5 && mouseY <= yPosition + 16;

		if (!mouseOver)
			return;

		mc.getTextureManager().bindTexture(new ResourceLocation("labymod/textures/misc/blocked.png"));
		drawUtils().drawTexture(maxX - (hoveringDelete ? 20 : 19), y + (hoveringDelete ? 3.5 : 4.5), 256, 256, hoveringDelete ? 16 : 14, hoveringDelete ? 16 : 14);

		mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/pencil.png"));
		drawUtils().drawTexture(maxX - (hoveringEdit ? 40 : 39), y + (hoveringEdit ? 3.5 : 4.5), 256, 256, hoveringEdit ? 16 : 14, hoveringEdit ? 16 : 14);
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
}
