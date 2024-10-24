/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.settings.types;

import com.google.gson.JsonNull;
import dev.l3g7.griefer_utils.core.settings.types.ButtonSetting;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Icon;
import dev.l3g7.griefer_utils.labymod.laby3.settings.Laby3Setting;
import net.labymod.settings.elements.ControlElement;
import net.minecraft.client.gui.GuiButton;

public class ButtonSettingImpl extends ControlElement implements Laby3Setting<ButtonSetting, Object>, ButtonSetting {

	private final ExtendedStorage<Object> storage = new ExtendedStorage<>(e -> JsonNull.INSTANCE, e -> NULL, NULL);
	private final GuiButton button = new GuiButton(-2, 0, 0, 23, 20, "");

	private Icon buttonIcon;

	public ButtonSettingImpl() {
		super("§cNo name set", null);
		setSettingEnabled(false);
	}

	@Override
	public ExtendedStorage<Object> getStorage() {
		return storage;
	}

	@Override
	public ButtonSetting buttonIcon(Object icon) {
		buttonIcon = Icon.of(icon);
		button.displayString = "";
		return this;
	}

	@Override
	public ButtonSetting buttonLabel(String label) {
		buttonIcon = null;
		button.displayString = label; // TODO fix button width
		return this;
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (!button.mousePressed(mc, mouseX, mouseY))
			return;

		button.playPressSound(mc.getSoundHandler());
		notifyChange();
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);

		mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;

		button.xPosition = maxX - 23 - 2;
		button.yPosition = y + 1;
		button.drawButton(mc, mouseX, mouseY);

		if (buttonIcon != null)
			buttonIcon.draw(button.xPosition + 1, button.yPosition, 14 / 16f);
	}

}
