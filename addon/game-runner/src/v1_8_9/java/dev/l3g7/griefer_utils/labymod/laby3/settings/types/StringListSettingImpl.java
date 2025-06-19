/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.settings.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.settings.types.StringListSetting;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Laby3Setting;
import net.labymod.core.LabyModCore;
import net.labymod.gui.elements.ModTextField;
import net.labymod.settings.LabyModModuleEditorGui;
import net.labymod.settings.PreviewRenderer;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Items;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class StringListSettingImpl extends ControlElement implements Laby3Setting<StringListSetting, List<String>>, StringListSetting {

	private final ExtendedStorage<List<String>> storage = new ExtendedStorage<>(list -> {
		JsonArray array = new JsonArray();
		list.forEach(s -> array.add(new JsonPrimitive(s)));
		return array;
	}, elem -> {
		List<String> list = new ArrayList<>();
		elem.getAsJsonArray().forEach(e -> list.add(e.getAsString()));
		return list;
	}, new ArrayList<>());

	public StringListSettingImpl() {
		super("§cEs gab einen Fehler!", null);
		setSettingEnabled(true);
	}

	private SettingsElement container = this;
	private StringAddSetting stringAddSetting = null;

	@Override
	public ExtendedStorage<List<String>> getStorage() {
		return storage;
	}

	@Override
	public void create(Object parent) {
		Laby3Setting.super.create(parent);
		this.container = (SettingsElement) parent;
		initList();
	}

	public void initList() {
		ArrayList<SettingsElement> settings = new ArrayList<>();
		for (String entry : get())
			settings.add(new StringDisplaySetting(entry));

		settings.add(stringAddSetting = new StringAddSetting());
		getSettings().remove(this);

		container.getSubSettings().addAll(settings);
	}

	@Override
	public StringListSetting set(List<String> value) {
		Laby3Setting.super.set(value);
		getSettings().removeIf(se -> se instanceof StringDisplaySetting);

		if (getSettings().contains(stringAddSetting))
			for (String s : value)
				getSettings().add(getSettings().indexOf(stringAddSetting), new StringDisplaySetting(s));

		return this;
	}

	@Override
	public boolean isFallbackValue() {
		return get() != null && get().isEmpty();
	}

	private List<SettingsElement> getSettings() {
		return container.getSubSettings().getElements();
	}

	private class StringDisplaySetting extends ListEntrySetting {

		private String data;

		public StringDisplaySetting(String entry) {
			super(true, true, false);
			container = StringListSettingImpl.this;
			icon(Items.paper);
			name(data = entry);
		}

		@Override
		protected void onChange() {
			StringListSettingImpl.this.get().remove(data);
			getSettings().remove(this);
			StringListSettingImpl.this.getStorage().callbacks.forEach(c -> c.accept(StringListSettingImpl.this.get()));
			StringListSettingImpl.this.save();
		}

		@Override
		protected void openSettings() {
			mc().displayGuiScreen(stringAddSetting.new AddStringGui(mc().currentScreen, this));
		}

	}

	private class StringAddSetting extends EntryAddSettingImpl {

		StringAddSetting() {
			name("Eintrag hinzufügen");
			callback(() -> mc().displayGuiScreen(new AddStringGui(mc().currentScreen, null)));
		}

		private class AddStringGui extends GuiScreen {

			private final GuiScreen backgroundScreen;
			private final StringDisplaySetting setting;
			private ModTextField inputField;

			public AddStringGui(GuiScreen backgroundScreen, StringDisplaySetting setting) {
				this.backgroundScreen = backgroundScreen;
				this.setting = setting;
			}

			public void initGui() {
				super.initGui();
				backgroundScreen.width = width;
				backgroundScreen.height = height;
				if (backgroundScreen instanceof LabyModModuleEditorGui)
					PreviewRenderer.getInstance().init(AddStringGui.class);

				inputField = new ModTextField(0, LabyModCore.getMinecraft().getFontRenderer(), width / 2 - 150, height / 4 + 45, 300, 20);
				inputField.setFocused(true);
				inputField.setMaxStringLength(100);
				if (setting != null) {
					inputField.setText(setting.data);
					inputField.setCursorPositionEnd();
				}

				buttonList.add(new GuiButton(0, width / 2 - 105, height / 4 + 85, 100, 20, "Abbrechen"));
				buttonList.add(new GuiButton(1, width / 2 + 5, height / 4 + 85, 100, 20, setting == null ? "Hinzufügen" : "Bearbeiten"));
			}

			@Override
			public void onGuiClosed() {
				EventRegisterer.unregister(this);
			}

			public void drawScreen(int mouseX, int mouseY, float partialTicks) {
				backgroundScreen.drawScreen(0, 0, partialTicks);
				drawRect(0, 0, width, height, Integer.MIN_VALUE);

				inputField.drawTextBox();

				super.drawScreen(mouseX, mouseY, partialTicks);
			}

			public void updateScreen() {
				backgroundScreen.updateScreen();
				inputField.updateCursorCounter();
			}

			protected void actionPerformed(GuiButton button) {
				super.actionPerformed(button);
				switch (button.id) {
					case 1:
						int lastIndex = getSettings().indexOf(StringAddSetting.this);
						if (setting == null) {
							getSettings().add(lastIndex, new StringDisplaySetting(inputField.getText()));
							StringListSettingImpl.this.get().add(inputField.getText());
						} else {
							setting.name(setting.data = inputField.getText());
							int settingIndex = getSettings().indexOf(setting);
							int listIndex = StringListSettingImpl.this.get().size() - (lastIndex - settingIndex);
							StringListSettingImpl.this.get().set(listIndex, inputField.getText());
						}

						StringListSettingImpl.this.save();
						StringListSettingImpl.this.getStorage().callbacks.forEach(c -> c.accept(StringListSettingImpl.this.get()));
						// Fall-through
					case 0:
						mc().displayGuiScreen(backgroundScreen);
						backgroundScreen.initGui(); // Update settings
				}
			}

			protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
				super.mouseClicked(mouseX, mouseY, mouseButton);
				inputField.mouseClicked(mouseX, mouseY, mouseButton);
			}

			protected void keyTyped(char typedChar, int keyCode) {
				if (keyCode == 1) // ESC
					mc().displayGuiScreen(backgroundScreen);

				inputField.textboxKeyTyped(typedChar, keyCode);
			}
		}

	}

	@Override
	public StringListSetting placeholder(String placeholder) {
		// TODO implement
		return this;
	}

	@Override
	public StringListSetting entryIcon(Object icon) {
		// TODO implement
		return this;
	}

}
