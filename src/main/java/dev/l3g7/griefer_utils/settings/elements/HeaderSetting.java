/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

import dev.l3g7.griefer_utils.settings.ElementBuilder;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;

/**
 * A setting to display text.
 */
public class HeaderSetting extends ControlElement implements ElementBuilder<HeaderSetting> {

	private int entryHeight = 22;
	private double scale = 1;

	public HeaderSetting() {
		this("§c");
	}

	public HeaderSetting(String name) {
		super(name, null);
	}

	public HeaderSetting scale(double scale) {
		this.scale = scale;
		return this;
	}

	public HeaderSetting entryHeight(int entryHeight) {
		this.entryHeight = entryHeight;
		return this;
	}

	@Override
	public int getObjectWidth() {
		return 9999999; // To suppress LabyMod focusing it when clicked
	}

	@Override
	public int getEntryHeight() {
		return entryHeight;
	}

	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		this.mouseOver = mouseX > x && mouseX < maxX && mouseY > y && mouseY < maxY;
		LabyMod.getInstance().getDrawUtils().drawCenteredString(getDisplayName(), x + (maxX - x) / 2d, y + 7, scale);
	}

}
