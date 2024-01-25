/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.settings.elements;

import net.labymod.main.LabyMod;

import java.util.ArrayList;
import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;

public class TextSetting extends HeaderSetting {

	private static final int FONT_HEIGHT = mc().fontRendererObj.FONT_HEIGHT;
	private static final List<String> lines = new ArrayList<>();
	private final int maxTextWidth;
	private int textWidth = 0;

	public TextSetting(int maxTextWidth) {
		super(null);
		this.maxTextWidth = maxTextWidth;
	}

	public TextSetting addText(String multiLineText) {
		for (String s : multiLineText.replace("\r", "").split("\n"))
			addEntry(s);

		return this;
	}

	private void addEntry(String text) {
		int stringWidth = mc().fontRendererObj.getStringWidth(text);

		if (stringWidth <= maxTextWidth) {
			lines.add(text);
			textWidth = Math.max(textWidth, stringWidth);
			return;
		}

		while (stringWidth > maxTextWidth) {

			String partString;

			// Limit the string to a width of 393
			for (int i = text.length(); true; i--) {
				partString = text.substring(0, i);

				if (mc().fontRendererObj.getStringWidth(partString) <= maxTextWidth)
					break;
			}

			// Chop up string at space
			int i = partString.length() - 1;
			while (true) {
				if (i < 0) {
					i = partString.length() - 1;
					break;
				}

				if (partString.charAt(i) == ' ')
					break;

				i--;
			}

			// Add the string and continue with the rest
			lines.add(text.substring(0, i));

			text = text.substring(i + 1);

			stringWidth = mc().fontRendererObj.getStringWidth(text);
		}

		lines.add(text);
		textWidth = maxTextWidth;
	}

	@Override
	public int getEntryHeight() {
		return super.getEntryHeight() + (lines.size() - 1) * (FONT_HEIGHT + 1);
	}

	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		for (String line : lines) {
			LabyMod.getInstance().getDrawUtils().drawString(line, x + (maxX - x) / 2d - (double) (textWidth / 2), y + 7, 1);
			y += FONT_HEIGHT + 1;
		}
	}

}