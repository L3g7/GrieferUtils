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

package dev.l3g7.griefer_utils.settings.elements.components;

import dev.l3g7.griefer_utils.settings.ElementBuilder;
import net.labymod.settings.elements.ControlElement;
import net.labymod.utils.ModColor;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.drawUtils;

public class EntryAddSetting extends ControlElement implements ElementBuilder<EntryAddSetting> {

	private Runnable callback;

	public EntryAddSetting() {
		this("§cno name set");
	}

	public EntryAddSetting(String displayName) {
		super(displayName, new IconData("labymod/textures/settings/category/addons.png"));
	}

	public EntryAddSetting callback(Runnable callback) {
		this.callback = callback;
		return this;
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;

		drawUtils().drawRectangle(x, y, maxX, maxY, ModColor.toRGB(80, 80, 80, 60));
		int iconWidth = iconData != null ? 25 : 2;
		mc.getTextureManager().bindTexture(iconData.getTextureIcon());

		if (mouseOver) {
			drawUtils().drawTexture(x + 2, y + 2, 256.0, 256.0, 18, 18);
			drawUtils().drawString(displayName, x + iconWidth + 1, (double) y + 7 - 0);
		} else {
			drawUtils().drawTexture(x + 3, y + 3, 256.0, 256.0, 16.0, 16.0);
			drawUtils().drawString(displayName, x + iconWidth, (double) y + 7 - 0);
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseOver)
			callback.run();
	}

}