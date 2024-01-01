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

package dev.l3g7.griefer_utils.settings.elements;

import dev.l3g7.griefer_utils.features.Module;
import dev.l3g7.griefer_utils.util.MinecraftUtil;
import net.labymod.gui.elements.Tabs;
import net.labymod.main.ModTextures;
import net.labymod.settings.LabyModModuleEditorGui;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.util.List;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;

public class ModuleProxySetting extends BooleanSetting {

	private final GuiButton button = new GuiButton(-2, 0, 0, 23, 20, "");
	private final Module module;

	public ModuleProxySetting(Module module) {
		this.module = module;
	}

	@Override
	public int getObjectWidth() {
		return 50;
	}

	@Override
	public String getDisplayName() {
		return module.getControlName();
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (button.mousePressed(mc, mouseX, mouseY)) {
			button.playPressSound(mc.getSoundHandler());
			Tabs.lastOpenScreen = null;
			mc.displayGuiScreen(new LabyModModuleEditorGui());
			List<SettingsElement> path = MinecraftUtil.path();
			path.add(Module.CATEGORY.getCategoryElement());
			path.add(module.getBooleanElement());
			return;
		}

		module.mainElement.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;
		button.xPosition = maxX - 75;
		button.yPosition = y + 1;

		module.mainElement.draw(x, y, maxX, maxY, mouseX, mouseY);

		if (!module.mainElement.getSubSettings().getElements().isEmpty())
			return;

		button.drawButton(mc, mouseX, mouseY);
		mc.getTextureManager().bindTexture(ModTextures.BUTTON_ADVANCED);
		GlStateManager.color(1, 1, 1, 1);
		drawUtils().drawTexture(maxX - 71, y + 4, 0, 0, 256, 256, 14, 14, 2);
	}

}