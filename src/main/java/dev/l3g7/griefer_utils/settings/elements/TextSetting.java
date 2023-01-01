/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
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

import net.labymod.main.LabyMod;
import net.labymod.utils.DrawUtils;
import net.minecraft.client.Minecraft;

public class TextSetting extends HeaderSetting {

	private static final int FONT_HEIGHT = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
	private double textSize = 1;

	public TextSetting() {
		this("§c");
	}

	public TextSetting(String name) {
		super(name);
	}

	public HeaderSetting scale(double scale) {
		textSize = scale;
		return this;
	}

	@Override
	public int getEntryHeight() {
		return super.getEntryHeight() + (displayName.split("\n").length - 1) * (FONT_HEIGHT + 1);
	}

	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		String text = displayName;
		displayName = null;
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		displayName = text;
		DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();

		int maxWidth = 0;
		for (String line : displayName.split("\n"))
			maxWidth = Math.max(maxWidth, drawUtils.getStringWidth(line));

		for (String line : displayName.split("\n")) {
			LabyMod.getInstance().getDrawUtils().drawString(line, x + (maxX - x) / 2d - (double) (maxWidth / 2), y + 7, textSize);
			y += FONT_HEIGHT + 1;
		}
	}

}