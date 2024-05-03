/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.settings.player_list;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.api.event.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.laby3.settings.Laby3Setting;
import dev.l3g7.griefer_utils.settings.BaseSetting;
import dev.l3g7.griefer_utils.v1_8_9.misc.gui.elements.laby_polyfills.DrawUtils;
import net.labymod.core.LabyModCore;
import net.labymod.gui.elements.ModTextField;
import net.labymod.main.ModTextures;
import net.labymod.settings.LabyModModuleEditorGui;
import net.labymod.settings.PreviewRenderer;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerListSettingLaby3 extends ControlElement implements Laby3Setting<PlayerListSettingLaby3, List<PlayerListEntry>> {

	private final ExtendedStorage<List<PlayerListEntry>> storage = new ExtendedStorage<>(list -> {
		JsonArray array = new JsonArray();
		list.forEach(e -> array.add(new JsonPrimitive(e.id)));
		return array;
	}, array -> {
		List<PlayerListEntry> list = new ArrayList<>();
		array.getAsJsonArray().forEach(e -> list.add(new PlayerListEntry(null, e.getAsString())));
		return list;
	}, new ArrayList<>());

	private SettingsElement container = this;

	public PlayerListSettingLaby3() {
		super("§cEs gab einen Fehler!", null);
		setSettingEnabled(true);
	}

	public void setContainer(SettingsElement container) {
		this.container = container;
	}

	@Override
	public PlayerListSettingLaby3 config(String configKey) {
		Laby3Setting.super.config(configKey);

		List<SettingsElement> settings = new ArrayList<>();
		for (PlayerListEntry entry : get())
			settings.add(new PlayerDisplaySetting(entry));

		PlayerAddSetting addSetting = new PlayerAddSetting();
		settings.add(addSetting);
		getSettings().remove(this);
		((Laby3Setting<?, ?>) container).subSettings(Reflection.<ArrayList<BaseSetting<?>>>c(settings));

		return this;
	}

	private List<SettingsElement> getSettings() {
		return container.getSubSettings().getElements();
	}

	@Override
	public int getObjectWidth() {
		return 0;
	}

	@Override
	public ExtendedStorage<List<PlayerListEntry>> getStorage() {
		return storage;
	}

	public boolean contains(String name, UUID uuid) {
		if (name == null && uuid == null)
			return false;

		for (PlayerListEntry entry : get())
			if (name == null ? uuid.toString().equalsIgnoreCase(entry.id) : name.equalsIgnoreCase(entry.name))
				return true;

		return false;
	}

	private class PlayerDisplaySetting extends ListEntrySetting {

		private final PlayerListEntry data;

		public PlayerDisplaySetting(PlayerListEntry entry) {
			super(true, false, false, new IconData(ModTextures.MISC_HEAD_QUESTION));
			container = PlayerListSettingLaby3.this.container;
			data = entry;
		}

		@Override
		protected void onChange() {
			get().remove(data);
			PlayerListSettingLaby3.this.save();
			getStorage().callbacks.forEach(c -> c.accept(get()));
		}

		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			setDisplayName(data.name == null ? "§cNutzer konnte nicht geladen werden!" : data.name);
			super.draw(x, y, maxX, maxY, mouseX, mouseY);
			DrawUtils.drawRectangle(x - 1, y, x, maxY, 0x78787878);

			renderSkull(data, x + 3, y + 3, 16);
		}

	}

	private class PlayerAddSetting extends EntryAddSetting {

		PlayerAddSetting() {
			super("Spieler hinzufügen");
			callback(() -> Minecraft.getMinecraft().displayGuiScreen(new AddPlayerGui(Minecraft.getMinecraft().currentScreen)));
		}

		private class AddPlayerGui extends GuiScreen {

			private final GuiScreen backgroundScreen;
			private ModTextField inputField;
			private GuiButton doneButton;
			private PlayerListEntry entry;

			public AddPlayerGui(GuiScreen backgroundScreen) {
				this.backgroundScreen = backgroundScreen;
				EventRegisterer.register(this);
			}

			public void initGui() {
				super.initGui();
				backgroundScreen.width = width;
				backgroundScreen.height = height;
				if (backgroundScreen instanceof LabyModModuleEditorGui)
					PreviewRenderer.getInstance().init(AddPlayerGui.class);

				inputField = new ModTextField(0, LabyModCore.getMinecraft().getFontRenderer(), width / 2 - 150, height / 4 + 45, 300, 20);
				inputField.setFocused(true);
				buttonList.add(new GuiButton(0, width / 2 - 105, height / 4 + 85, 100, 20, "Abbrechen"));
				buttonList.add(doneButton = new GuiButton(1, width / 2 + 5, height / 4 + 85, 100, 20, "Hinzufügen"));
			}

			@Override
			public void onGuiClosed() {
				EventRegisterer.unregister(this);
			}

			private void updateValidity() {
				entry = PlayerListEntry.getEntry(inputField.getText());

				if (!entry.exists) {
					inputField.setTextColor(0xFFFF0000);
					doneButton.enabled = false;
				} else {
					inputField.setTextColor(0xFFFFFFFF);
					doneButton.enabled = entry.loaded;
				}
			}

			public void drawScreen(int mouseX, int mouseY, float partialTicks) {
				backgroundScreen.drawScreen(0, 0, partialTicks);
				drawRect(0, 0, width, height, Integer.MIN_VALUE);

				updateValidity();
				inputField.drawTextBox();

				super.drawScreen(mouseX, mouseY, partialTicks);
				renderSkull(entry, (width - 32) / 2, height / 4, 32);
			}

			public void updateScreen() {
				backgroundScreen.updateScreen();
				inputField.updateCursorCounter();
			}

			protected void actionPerformed(GuiButton button) {
				super.actionPerformed(button);
				switch (button.id) {
					case 1:
						getSettings().add(getSettings().indexOf(PlayerAddSetting.this), new PlayerDisplaySetting(entry));
						PlayerListSettingLaby3.this.get().add(entry);
						save();
						getStorage().callbacks.forEach(c -> c.accept(get()));
						// Fall-through
					case 0:
						Minecraft.getMinecraft().displayGuiScreen(backgroundScreen);
						backgroundScreen.initGui(); // Update settings
				}
			}

			protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
				super.mouseClicked(mouseX, mouseY, mouseButton);
				inputField.mouseClicked(mouseX, mouseY, mouseButton);
			}

			protected void keyTyped(char typedChar, int keyCode) {
				if (keyCode == 1) // ESC
					Minecraft.getMinecraft().displayGuiScreen(backgroundScreen);

				inputField.textboxKeyTyped(typedChar, keyCode);
				updateValidity();
			}
		}

	}

	private void renderSkull(PlayerListEntry e, double x, double y, int size) {
		if (e.skin == null) {
			mc.getTextureManager().bindTexture(ModTextures.MISC_HEAD_QUESTION);
			DrawUtils.drawTexture(x, y, 0, 0, 256, 256, size, size);
			return;
		}

		GlStateManager.bindTexture(e.skin.getGlTextureId());

		if (!e.isMojang()) {
			DrawUtils.drawTexture(x, y, 0, 0, 256, 256, size, size);
			return;
		}

		int yHeight = e.oldSkin ? 64 : 32; // Old textures are 32x64
		DrawUtils.drawTexture(x, y, 32, yHeight, 32, yHeight, size, size); // First layer
		DrawUtils.drawTexture(x, y, 160, yHeight, 32, yHeight, size, size); // Second layer
	}

}
