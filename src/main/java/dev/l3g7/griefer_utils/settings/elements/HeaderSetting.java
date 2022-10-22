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
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.settings.elements.HeaderElement;

/**
 * A setting to display text.
 */
public class HeaderSetting extends HeaderElement implements ElementBuilder<HeaderSetting> {

	private int entryHeight = super.getEntryHeight();

	public HeaderSetting() {
		super("§c");
	}

	public HeaderSetting(String name) {
		super(name);
	}

	public HeaderSetting scale(double scale) {
		Reflection.set(this, scale, "textSize");
		return this;
	}

	public HeaderSetting entryHeight(int entryHeight) {
		this.entryHeight = entryHeight;
		return this;
	}

	@Override
	public int getEntryHeight() {
		return entryHeight;
	}

}
