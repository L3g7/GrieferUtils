/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.ingoing.chat_menu.impl.laby3;

import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.misc.gui.elements.laby_polyfills.DrawUtils;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.SwitchSettingImpl;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

public class CopyDisplaySettingLaby3 extends SwitchSettingImpl {

	private final SwitchSetting settingContainer;

	public CopyDisplaySettingLaby3(SwitchSetting settingContainer) {
		this.settingContainer = settingContainer;
	}

	private boolean editHovered = false;

	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (editHovered) {
			Reflection.<ArrayList<BaseSetting<?>>>get(mc.currentScreen, "path").add(settingContainer);
			mc.currentScreen.initGui();
		}
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);

		editHovered = mouseX > maxX - 70 && mouseX < maxX - 55 && mouseY > y + 4 && mouseY < y + 20;
		mc.getTextureManager().bindTexture(new ResourceLocation("griefer_utils/icons/pencil.png"));
		DrawUtils.drawTexture(maxX - 66 - (editHovered ? 4 : 3), y + (editHovered ? 3.5 : 4.5), 256, 256, editHovered ? 16 : 14, editHovered ? 16 : 14);
	}

}
