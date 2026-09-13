/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.settings.types.list;

import dev.l3g7.griefer_utils.core.api.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.settings.types.list.StringListEntry;
import net.labymod.core.LabyModCore;
import net.labymod.gui.elements.ModTextField;
import net.labymod.settings.LabyModModuleEditorGui;
import net.labymod.settings.PreviewRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class StringListSettingImpl extends ListSettingImpl<StringListEntry> {

	public StringListSettingImpl() {
		super(StringListEntry.class);
		customEdit(e -> mc().displayGuiScreen(new AddStringGui(mc().currentScreen, e)));
	}

	private class AddStringGui extends GuiScreen {

		private final GuiScreen backgroundScreen;
		private final StringListEntry entry;
		private ModTextField inputField;

		public AddStringGui(GuiScreen backgroundScreen, StringListEntry entry) {
			this.backgroundScreen = backgroundScreen;
			this.entry = entry;
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
			if (entry != null) {
				inputField.setText(entry.toString());
				inputField.setCursorPositionEnd();
			}

			buttonList.add(new GuiButton(0, width / 2 - 105, height / 4 + 85, 100, 20, "Abbrechen"));
			buttonList.add(new GuiButton(1, width / 2 + 5, height / 4 + 85, 100, 20, entry == null ? "Hinzufügen" : "Bearbeiten"));
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
					if (entry == null)
						add(new StringListEntry(inputField.getText()));
					else
						entry.set(inputField.getText());

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
		}
	}

}
