/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.item.recraft.laby3;

import net.labymod.main.LabyMod;
import net.labymod.main.ModTextures;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

public class SmallButtonSetting extends ControlElement {

	private final GuiButton button = new GuiButton(-2, 0, 0, 23, 20, "");
	private final List<Runnable> callbacks = new ArrayList<>();
	private IconData buttonIcon = new IconData(ModTextures.BUTTON_ADVANCED);

	public SmallButtonSetting(IconData i) {
		super("§cNo name set", i);
		setSettingEnabled(false);
	}

	public SmallButtonSetting callback(Runnable runnable) {
		callbacks.add(runnable);
		return this;
	}

	public SmallButtonSetting buttonIcon(IconData icon) {
		buttonIcon = icon;
		return this;
	}

	@Override
	public int getObjectWidth() {
		return 0;
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (!button.mousePressed(mc, mouseX, mouseY))
			return;

		button.playPressSound(mc.getSoundHandler());
		callbacks.forEach(Runnable::run);
	}

	protected void drawButtonIcon(IconData buttonIcon, int buttonX, int buttonY) {
		if (buttonIcon.hasMaterialIcon()) {
			LabyMod.getInstance().getDrawUtils().drawItem(buttonIcon.getMaterialIcon().createItemStack(), buttonX + 3, buttonY + 2, null);
			return;
		}

		GlStateManager.enableBlend();
		GlStateManager.color(1, 1, 1);
		mc.getTextureManager().bindTexture(buttonIcon.getTextureIcon());
		LabyMod.getInstance().getDrawUtils().drawTexture(buttonX + 4, buttonY + 3, 0, 0, 256, 256, 14, 14, 2);
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);

		mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;

		button.xPosition = maxX - 23 - 2;
		button.yPosition = y + 1;
		button.drawButton(mc, mouseX, mouseY);

		if (buttonIcon != null && buttonIcon.hasMaterialIcon() != buttonIcon.hasTextureIcon())
			drawButtonIcon(buttonIcon, button.xPosition, button.yPosition);
	}

}