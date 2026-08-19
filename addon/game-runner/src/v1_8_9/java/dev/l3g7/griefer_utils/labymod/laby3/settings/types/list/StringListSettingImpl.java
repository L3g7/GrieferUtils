/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.settings.types.list;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Consumer;
import dev.l3g7.griefer_utils.core.settings.types.list.ListSetting;
import dev.l3g7.griefer_utils.core.settings.types.list.StringListEntry;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Laby3Setting;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.EntryAddSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.ListEntrySetting;
import net.labymod.core.LabyModCore;
import net.labymod.gui.elements.ModTextField;
import net.labymod.settings.LabyModModuleEditorGui;
import net.labymod.settings.PreviewRenderer;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class StringListSettingImpl extends ControlElement implements Laby3Setting<ListSetting<StringListEntry>, List<StringListEntry>>, ListSetting<StringListEntry> {

	private final ExtendedStorage<List<StringListEntry>> storage = new ExtendedStorage<>(list -> {
		JsonArray array = new JsonArray();
		list.forEach(s -> array.add(new JsonPrimitive(s.toString())));
		return array;
	}, elem -> {
		List<StringListEntry> list = new ArrayList<>();
		elem.getAsJsonArray().forEach(e -> list.add(new StringListEntry(e.getAsString())));
		return list;
	}, new ArrayList<>());

	public StringListSettingImpl() {
		super("§cEs gab einen Fehler!", null);
		setSettingEnabled(true);
	}

	private boolean unpacked = false;
	private SettingsElement container = this;
	private StringAddSetting stringAddSetting = null;

	@Override
	public ExtendedStorage<List<StringListEntry>> getStorage() {
		return storage;
	}

	@Override
	public void create(Object parent) {
		if (!unpacked)
			throw new UnsupportedOperationException("Packed lists are not implemented.");

		Laby3Setting.super.create(parent);
		this.container = (SettingsElement) parent;
		initList();
	}

	public void initList() {
		ArrayList<SettingsElement> settings = new ArrayList<>();
		for (StringListEntry entry : get())
			settings.add(new StringDisplaySetting(entry));

		settings.add(stringAddSetting = new StringAddSetting());
		getSettings().remove(this);

		container.getSubSettings().addAll(settings);
	}

	private List<StringListEntry> getAsList() {
		return ((List<StringListEntry>) get());
	}

	@Override
	public void add(StringListEntry value) {
		getSettings().add(new StringDisplaySetting(value));
	}

	@Override
	public ListSetting<StringListEntry> customEdit(Consumer<StringListEntry> callback) {
		// No-op
		return this;
	}

	@Override
	public ListSetting<StringListEntry> unpacked() {
		this.unpacked = true;
		return this;
	}

	private List<SettingsElement> getSettings() {
		return container.getSubSettings().getElements();
	}

	private class StringDisplaySetting extends ListEntrySetting {

		private final StringListEntry data;

		public StringDisplaySetting(StringListEntry entry) {
			super(true, true, false);
			container = StringListSettingImpl.this;
			icon("book_and_quill");
			name(entry.toString());
			data = entry;
		}

		@Override
		protected void onChange() {
			StringListSettingImpl.this.getAsList().remove(data);
			getSettings().remove(this);
			StringListSettingImpl.this.notifyChange();
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
					inputField.setText(setting.data.toString());
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
							var entry = new StringListEntry(inputField.getText());
							getSettings().add(lastIndex, new StringDisplaySetting(entry));
							StringListSettingImpl.this.add(entry);
						} else {
							setting.data.set(inputField.getText());
							setting.name(inputField.getText());
						}

						StringListSettingImpl.this.notifyChange();
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

}
