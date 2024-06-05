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

package dev.l3g7.griefer_utils.features.modules.laby3.balances.inventory_value;

import dev.l3g7.griefer_utils.core.api.misc.Constants;
import net.labymod.core.LabyModCore;
import net.labymod.gui.elements.ModTextField;
import net.labymod.main.lang.LanguageManager;
import net.labymod.utils.Consumer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class EnterItemValueGui extends GuiScreen {

	private final GuiScreen previousScreen = mc.currentScreen;
	private final GuiScreen targetScreen;
	private final Consumer<Long> callback;
	private ModTextField textField = null;
	private long number;

	public EnterItemValueGui(Consumer<Long> callback, GuiScreen targetScreen, long startValue) {
		this.callback = callback;
		this.targetScreen = targetScreen;
		this.number = startValue;
	}

	public void initGui() {
		super.initGui();

		String previousText;
		if (textField != null)
			previousText = textField.getText();
		else
			previousText = number != -1 ? Constants.DECIMAL_FORMAT_98.format(number) : "";

		textField = new ModTextField(0, LabyModCore.getMinecraft().getFontRenderer(), width / 2 - 150, height / 4 + 45, 300, 20);
		textField.setText(previousText);
		textField.getSelectionEnd();
		textField.setCursorPositionEnd();
		textField.setFocused(true);
		buttonList.add(new GuiButton(1, width / 2 - 125, height / 4 + 85, 100, 20, LanguageManager.translate("button_cancel")));
		buttonList.add(new GuiButton(2, width / 2 + 25, height / 4 + 85, 100, 20, LanguageManager.translate("button_done")));
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawRect(0, 0, width, height, Integer.MIN_VALUE);
		drawRect(width / 2 - 165, height / 4 + 20, width / 2 + 165, height / 4 + 120, Integer.MIN_VALUE);
		drawCenteredString(fontRendererObj, "Bitte gib an, wie viel das Item wert ist:", width / 2, height / 4 + 28, Integer.MAX_VALUE);
		textField.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		textField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	protected void keyTyped(char typedChar, int keyCode) {
		if (keyCode == 1)
			mc.currentScreen = previousScreen;

		if (typedChar == '-')
			return;

		String previousText = textField.getText();

		if (keyCode == 211 // Account for delete in front of a dot
			&& textField.getCursorPosition() != textField.getText().length()
			&& textField.getText().charAt(textField.getCursorPosition()) == '.')
			textField.setCursorPosition(textField.getCursorPosition() + 1);

		if (keyCode == 14 // Account for backspace behind a dot
			&& textField.getCursorPosition() > 0
			&& textField.getText().charAt(textField.getCursorPosition() - 1) == '.')
			textField.setCursorPosition(textField.getCursorPosition() - 1);

		if (keyCode == 28) {
			if (!textField.getText().isEmpty()) {
				callback.accept(number);
				mc.displayGuiScreen(targetScreen);
			}
			return;
		}

		if (typedChar == 'k') {
			textField.textboxKeyTyped('0', 48);
			textField.textboxKeyTyped('0', 48);
			textField.textboxKeyTyped('0', 48);
		} else if (!textField.textboxKeyTyped(typedChar, keyCode))
			return;

		String text = textField.getText().replace(".", "");
		try {
			number = Long.parseLong(text);
		} catch (NumberFormatException ignored) {}

		if (textField.getText().isEmpty())
			return;

		int cursorPos = textField.getCursorPosition();
		int selectionEnd = textField.getSelectionEnd();
		textField.setText(Constants.DECIMAL_FORMAT_98.format(number));

		int delta = (textField.getText().length() - previousText.length()) / 2;
		cursorPos += delta;
		selectionEnd += delta;

		textField.setCursorPosition(cursorPos);
		textField.setSelectionPos(selectionEnd);
	}

	public void updateScreen() {
		textField.updateCursorCounter();
	}

	protected void actionPerformed(GuiButton button) {
		if (button.id == 2 && !textField.getText().isEmpty()) {
			callback.accept(number);
			mc.displayGuiScreen(targetScreen);
			return;
		}

		if (button.id == 1)
			mc.currentScreen = previousScreen;
	}

}