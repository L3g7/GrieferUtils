/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.world;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.features.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;

import java.util.Arrays;

import static org.lwjgl.input.Keyboard.*;

/**
 * Implements cursor movement, selection and copy and paste in the sign edit gui.
 */
@Singleton
public class BetterSign extends Feature {

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Schilder verbessern")
		.description("Fügt Unterstützung für Cursorbewegung, Auswahl und Copy & Paste bei Schildern hinzu.")
		.icon(Items.sign);

	@EventListener
	public void onGuiOpen(GuiOpenEvent<GuiEditSign> event) {
		event.gui = new BetterGuiEditSign(Reflection.get(event.gui, "tileSign"));
	}

	/**
	 * The gui with the patches applied.
	 */
	private static class BetterGuiEditSign extends GuiEditSign {

		private final TileEntitySign tileSign;
		private final SignTextField[] lines = new SignTextField[4];
		private boolean allSelected = false;

		public BetterGuiEditSign(TileEntitySign tileSign) {
			super(tileSign);
			this.tileSign = tileSign;
		}

		@Override
		public void initGui() {
			super.initGui();

			// Initialize text fields
			for (int i = 0; i < 4; i++) {
				lines[i] = new SignTextField(-1, Minecraft.getMinecraft().fontRendererObj, 0, 2 + 10 * i, 200, 10);
				lines[i].setText(tileSign.signText[i].getUnformattedText());
			}
			lines[0].setFocused(true);
		}

		@Override
		public void updateScreen() {
			super.updateScreen();
			Arrays.stream(lines).forEach(SignTextField::updateCursorCounter);
		}

		/**
		 * Copies the selected text to the clipboard.
		 */
		private void copySelectedText() {
			if (allSelected) {
				StringBuilder text = new StringBuilder();
				for (SignTextField field : lines)
					text.append(field.getSelectedText()).append("\n");

				setClipboardString(text.toString().replaceAll("[\r\n]+$", ""));
			} else {
				setClipboardString(lines[getEditLine()].getSelectedText());
			}
		}

		/**
		 * Resets the selection for all lines.
		 */
		private void resetSelection() {
			allSelected = false;
			for (SignTextField line : lines)
				line.setSelectionPos(line.getCursorPosition());
		}

		@Override
		protected void keyTyped(char typedChar, int keyCode) {
			int editLine = getEditLine();
			SignTextField line = lines[editLine];

			// Process copy & paste
			if (isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown()) {
				if (keyCode == KEY_A) {
					// Select everything
					// If line is already selected, select all lines
					if (line.getText().equals(line.getSelectedText())) {
						for (SignTextField field : lines) {
							field.setCursorPositionZero();
							field.setSelectionPos(line.getText().length());
						}
						allSelected = true;
					} else {
						line.setCursorPositionZero();
						line.setSelectionPos(line.getText().length());
					}
					return;
				}
				else if (keyCode == KEY_C) {
					// Copy selected
					copySelectedText();
					return;
				}
				else if (keyCode == KEY_X) {

					// Copy and remove selected
					copySelectedText();
					for (int i = 0; i < lines.length; i++) {
						lines[i].writeText("");
						tileSign.signText[i] = new ChatComponentText(lines[i].getText());
					}
					return;
				}
				else if (keyCode == KEY_V) {

					// Paste selected (supports multiline)
					String[] strings = getClipboardString().split("\r?\n|\r");

					int i = 0;
					int l;
					for (l = editLine; l < lines.length; l++) {
						lines[l].writeText(strings[i]);
						lines[l].setText(fontRendererObj.trimStringToWidth(lines[l].getText(), 90));
						tileSign.signText[l] = new ChatComponentText(lines[l].getText());
						if (++i == strings.length)
							break;
					}
					setEditLine(Math.min(l, 3)); // Set selected line to last line where stuff was pasted
					return;
				}
			}

			// Reset selections if moving line or everything selected
			if (keyCode == KEY_UP || keyCode == KEY_DOWN || keyCode == KEY_RETURN || keyCode == KEY_NUMPADENTER)
				resetSelection();

			// Move cursor to end if moving line using the enter key
			if (keyCode == KEY_RETURN || keyCode == KEY_NUMPADENTER)
				line.setCursorPositionEnd();

			// Process key input
			super.keyTyped(typedChar, keyCode);
			editLine = getEditLine();

			String oldText = line.getText();
			if (line.textboxKeyTyped(typedChar, keyCode)) {
				// Update selection
				if (allSelected) {
					resetSelection();
					if (!line.getText().equals(oldText)) {
						for (int i = 0; i < 4; i++)
							lines[i].setText("");
						line.textboxKeyTyped(typedChar, keyCode);
					}
				}

				// Force size limit
				if (fontRendererObj.getStringWidth(line.getText()) > 90)
					line.deleteFromCursor(-1);

				// Update
				tileSign.signText[editLine] = new ChatComponentText(line.getText());
			}
		}

		@Override
		protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
			super.mouseClicked(mouseX, mouseY, mouseButton);
			int offset = tileSign.getBlockType() == Blocks.standing_sign ? 70 : 100;
			lines[getEditLine()].setCursorPositionEnd();
			int editLine = MathHelper.clamp_int((mouseY - offset) / 10, 0, 3);
			lines[editLine].mouseClicked(mouseX, mouseButton, width);
			setEditLine(editLine);
		}

		@Override
		public void drawScreen(int mouseX, int mouseY, float partialTicks) {
			// Hide text on sign
			for (int i = 0; i < 4; i++)
				tileSign.signText[i] = new ChatComponentText("");
			int editLine = getEditLine();
			setEditLine(-1);

			super.drawScreen(mouseX, mouseY, partialTicks);

			// Add text back to sign
			setEditLine(editLine);
			for (int i = 0; i < 4; i++)
				tileSign.signText[i] = new ChatComponentText(lines[i].getText());

			GlStateManager.pushMatrix();
			GlStateManager.translate((float) (width / 2), -0.5F, 500.0F);

			// Account for extra offset when creating a standing sign
			if (tileSign.getBlockType() == Blocks.standing_sign)
				GlStateManager.translate(0.0F, 70, 0.0F);
			else
				GlStateManager.translate(0.0F, 100, 0.0F);

			// Draw text fields
			int i = 0;
			for (SignTextField line : lines) {
				int w = fontRendererObj.getStringWidth(line.getText());
				line.xPosition = -w / 2;
				line.drawTextBox();
				line.setFocused(getEditLine() == i++);
			}

			GlStateManager.popMatrix();
		}

		private int getEditLine() {
			return Reflection.get(this, "editLine");
		}

		private void setEditLine(int editLine) {
			Reflection.set(this, "editLine", editLine);
		}

	}

	/**
	 * A custom {@link GuiTextField} with the sign's special cursor and centered text.
	 */
	private static class SignTextField extends GuiTextField {

		private final FontRenderer font;
		private int cursorCounter;

		public SignTextField(int componentId, FontRenderer font, int x, int y, int width, int height) {
			super(componentId, font, x, y, width, height);
			this.font = font;
		}

		public void mouseClicked(int mouseX, int mouseButton, int width) {
			int center = width / 2;
			if (mouseButton == 0) {
				int clickPos = mouseX - center - xPosition;
				int cursorPos = font.trimStringToWidth(getText(), clickPos).length();

				// If distance to next gap is smaller, set cursor pos to it
				if (getText().length() != cursorPos && Math.abs(font.getStringWidth(getText().substring(0, cursorPos)) - clickPos) > Math.abs(font.getStringWidth(getText().substring(0, cursorPos + 1)) - clickPos))
					cursorPos++;

				setCursorPosition(cursorPos);
			}
		}

		public void drawTextBox() {
			String text = getText();
			int cursorPos = getCursorPosition();
			int selectionEnd = Math.min(getSelectionEnd(), text.length());
			boolean showCursor = isFocused() && cursorCounter / 6 % 2 == 0;
			boolean cursorInText = cursorPos < text.length(); // Whether the cursor is not at the end
			int textEnd = xPosition; // The position of the text end

			// Draw the text
			if (text.length() > 0) {
				textEnd = font.drawString(text.substring(0, cursorPos), xPosition, yPosition, 0);
				font.drawString(text.substring(cursorPos), textEnd, yPosition, 0);
			}

			// Draw cursor
			if (showCursor) {
				if (cursorInText) {
					Gui.drawRect(textEnd - 1, yPosition - 1, textEnd, yPosition + 1 + font.FONT_HEIGHT, 0xFF000000);
				} else {
					font.drawString("> ", xPosition - font.getStringWidth("> "), yPosition, 0);
					font.drawString(" <", textEnd, yPosition, 0);
				}
			}

			// Draw selection
			if (selectionEnd != cursorPos) {
				int l1 = xPosition + font.getStringWidth(text.substring(0, selectionEnd));
				Reflection.invoke(this, "drawCursorVertical", textEnd, yPosition - 1, l1 - 1, yPosition + 1 + font.FONT_HEIGHT);
			}
		}

		@Override
		public void updateCursorCounter() {
			super.updateCursorCounter();
			++cursorCounter;
		}

	}

}
