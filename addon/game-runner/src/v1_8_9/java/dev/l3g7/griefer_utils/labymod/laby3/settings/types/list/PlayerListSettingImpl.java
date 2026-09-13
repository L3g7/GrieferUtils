/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.settings.types.list;

import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.misc.player_resolver.PlayerListEntry;
import net.labymod.core.LabyModCore;
import net.labymod.gui.elements.ModTextField;
import net.labymod.main.ModTextures;
import net.labymod.settings.LabyModModuleEditorGui;
import net.labymod.settings.PreviewRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.UUID;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class PlayerListSettingImpl extends ListSettingImpl<PlayerListEntry> {

	public PlayerListSettingImpl() {
		super(PlayerListEntry.class);
		customEdit(e -> mc().displayGuiScreen(new AddPlayerGui(mc().currentScreen)));
		addSetting.name("Spieler hinzufügen");
	}

	public boolean contains(String name, UUID uuid) {
		if (name == null && uuid == null)
			return false;

		for (PlayerListEntry entry : get())
			if (name == null ? uuid.toString().equalsIgnoreCase(entry.getId()) : name.equalsIgnoreCase(entry.getName()))
				return true;

		return false;
	}

	@Override
	protected DisplaySetting<PlayerListEntry> createDisplaySetting(PlayerListEntry entry) {
		return new PlayerDisplaySetting(this, entry);
	}

	private class PlayerDisplaySetting extends DisplaySetting<PlayerListEntry> {

		public PlayerDisplaySetting(ListSettingImpl<PlayerListEntry> parent, PlayerListEntry entry) {
			super(parent, entry);
		}

		@Override
		public void build() {
			icon("barrier");
		}

		@Override
		public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
			setDisplayName(data.getName() == null ? "§cNutzer konnte nicht geladen werden!" : data.getName());
			super.draw(x, y, maxX, maxY, mouseX, mouseY);
			DrawUtils.drawRectangle(x - 1, y, x, maxY, 0x78787878);

			renderSkull(data, x + 3, y + 3, 16);
		}

	}

	private class AddPlayerGui extends GuiScreen {

		private final GuiScreen backgroundScreen;
		private PlayerListEntry entry;
		private ModTextField inputField;
		private GuiButton doneButton;

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
			entry = PlayerListEntry.fromName(inputField.getText());

			if (!entry.isValid()) {
				inputField.setTextColor(0xFFFF0000);
				doneButton.enabled = false;
			} else {
				inputField.setTextColor(0xFFFFFFFF);
				doneButton.enabled = entry.isLoaded();
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
					add(entry);
					notifyChange();
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
			updateValidity();
		}
	}

	private void renderSkull(PlayerListEntry e, double x, double y, int size) {
		if (e.getSkin() == null) {
			mc.getTextureManager().bindTexture(ModTextures.MISC_HEAD_QUESTION);
			DrawUtils.drawTexture(x, y, 0, 0, 256, 256, size, size);
			return;
		}

		DrawUtils.bindTexture(e.getSkin());

		if (!e.isJava()) {
			DrawUtils.drawTexture(x, y, 0, 0, 256, 256, size, size);
			return;
		}

		int yHeight = e.skinHeight();
		DrawUtils.drawTexture(x, y, 32, yHeight, 32, yHeight, size, size); // First layer
		DrawUtils.drawTexture(x, y, 160, yHeight, 32, yHeight, size, size); // Second layer
	}

}
