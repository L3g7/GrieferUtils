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

package dev.l3g7.griefer_utils.features.chat.multi_hotkey;

import com.google.common.collect.ImmutableSet;
import dev.l3g7.griefer_utils.core.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.util.Util;
import dev.l3g7.griefer_utils.event.events.MessageEvent;
import dev.l3g7.griefer_utils.misc.gui.guis.AddonsGuiWithCustomBackButton;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.elements.*;
import dev.l3g7.griefer_utils.util.ItemUtil;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.Material;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.*;

public class HotkeyDisplaySetting extends ControlElement implements ElementBuilder<HotkeyDisplaySetting> {

	private final IconStorage iconStorage = new IconStorage();

	public final StringSetting name;
	public final KeySetting keys;
	public final StringListSetting commands;
	public final ItemSetting cityBuild;

	private String defaultName;
	private Set<Integer> defaultKeys;
	private List<String> defaultCommands;
	private ItemStack defaultCityBuild;

	private int amountsTriggered = 0;
	private boolean hoveringDelete = false;
	private boolean hoveringEdit = false;

	public HotkeyDisplaySetting(String name, Set<Integer> keys, List<String> commands, ItemStack cityBuild) {
		super("§f", null);

		this.name = new StringSetting()
			.name("Name")
			.description("Wie dieser Hotkey heißen soll.")
			.defaultValue(defaultName = name)
			.icon(Material.BOOK_AND_QUILL);

		this.commands = new StringListSetting()
			.name("Befehl hinzufügen")
			.defaultValue(defaultCommands = commands)
			.setContainer(this);

		this.cityBuild = new ItemSetting(ItemUtil.CB_ITEMS, false)
			.name("CityBuild")
			.description("Auf welchem Citybuild dieser Eintrag angezeigt werden soll.")
			.defaultValue((defaultCityBuild = cityBuild) == null ? ItemUtil.CB_ITEMS.get(0) : defaultCityBuild);

		this.keys = new KeySetting()
			.name("Taste")
			.description("Durch das Drücken welcher Taste/-n dieser Hotkey ausgelöst wird.")
			.defaultValue(defaultKeys = keys)
			.icon("key")
			.pressCallback(b -> {
				if (!b || !FileProvider.getSingleton(MultiHotkey.class).isEnabled())
					return;

				String cb = this.cityBuild.get().getDisplayName();
				if (!cb.equals("Egal") && !cb.equals(MinecraftUtil.getServerFromScoreboard()))
					return;

				if (this.commands.get().size() == 0) {
					displayAchievement("§cFehler", "§cBitte füge den Eintrag neu hinzu.");
					return;
				}

				String command = this.commands.get().get(amountsTriggered %= this.commands.get().size());
				if (!MessageEvent.MessageSendEvent.post(command))
					player().sendChatMessage(command);

				amountsTriggered++;
			});


		subSettings(this.name, this.keys, this.commands, this.cityBuild, new HeaderSetting("§e§lBefehle").scale(0.7));
		setSettingEnabled(false);
		this.commands.initList();
	}

	@Override
	public IconStorage getIconStorage() {
		return iconStorage;
	}

	public void openSettings() {
		defaultName = name.get();
		defaultKeys = keys.get();
		defaultCommands = new ArrayList<>(commands.get());
		defaultCityBuild = cityBuild.get();
		mc.displayGuiScreen(new AddonsGuiWithCustomBackButton(() -> {
			if (!name.get().isEmpty() && !keys.get().isEmpty() && !commands.get().isEmpty() && cityBuild.get() != null) {
				triggerOnChange();
				return;
			}

			if (defaultName.isEmpty()) {
				remove();
				return;
			}

			if (name.get().isEmpty())
				name.set(defaultName);
			if (keys.get().isEmpty())
				keys.set(defaultKeys);
			if (commands.get().isEmpty())
				commands.set(defaultCommands);
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
		FileProvider.getSingleton(MultiHotkey.class).getMainElement().getSubSettings().getElements().remove(this);
		keys.set(ImmutableSet.of());
		triggerOnChange();
	}

	private void triggerOnChange() {
		icon(cityBuild.get());
		FileProvider.getSingleton(MultiHotkey.class).onChange();
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		hideSubListButton();
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		drawIcon(x, y);

		mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;

		String subtitle = String.format("§e[%s] §f§o➡ %s", Util.formatKeys(keys.get()), commands.get().size() + (commands.get().size() == 1 ? " Befehl" : " Befehle"));

		String trimmedName = drawUtils().trimStringToWidth(name.get(), maxX - x - 25 - 48);
		String trimmedSubtitle = drawUtils().trimStringToWidth(subtitle, maxX - x - 25 - 48);
		drawUtils().drawString(trimmedName + (trimmedName.equals(name.get()) ? "" : "…"), x + 25, y + 7 - 5);
		drawUtils().drawString(trimmedSubtitle + (trimmedSubtitle.equals(subtitle) ? "" : "…"), x + 25, y + 7 + 5);

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

}
