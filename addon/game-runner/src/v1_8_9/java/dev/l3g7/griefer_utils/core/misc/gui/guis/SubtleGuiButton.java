/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.gui.guis;

import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class SubtleGuiButton extends GuiButton {

	public SubtleGuiButton(int id, double x, double y, String text) {
		super(id, (int) x, (int) y, DrawUtils.getStringWidth(text), mc().fontRendererObj.FONT_HEIGHT, text);
	}

	private boolean isHovered(int mouseX, int mouseY) {
		double left = xPosition - 20;
		double right = xPosition + width + 20;
		double top = yPosition - 5;
		double bottom = yPosition + height + 4;

		return mouseX > left
			&& mouseX < right
			&& mouseY > top
			&& mouseY < bottom;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (!visible)
			return;

		String text = "§" + (hovered ? 'c' : '7') + displayString;
		DrawUtils.drawString(text, xPosition, yPosition);
		hovered = isHovered(mouseX, mouseY);
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		return enabled && visible && isHovered(mouseX, mouseY);
	}
}
