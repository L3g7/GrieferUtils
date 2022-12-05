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

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.ValueHolder;
import net.labymod.settings.elements.NumberElement;

/**
 * A setting holding an integer.
 */
public class NumberSetting extends NumberElement implements ElementBuilder<NumberSetting>, ValueHolder<NumberSetting, Integer> {

	private final IconStorage iconStorage = new IconStorage();
	private final Storage<Integer> storage = new Storage<>(JsonPrimitive::new, JsonElement::getAsInt, 0);

	public NumberSetting() {
		super("§cNo name set", null, 0);
		addCallback(this::set);
	}

	@Override
	public Storage<Integer> getStorage() {
		return storage;
	}

	@Override
	public IconStorage getIconStorage() {
		return iconStorage;
	}

	/**
	 * Sets the lower limit the value can have.
	 */
	public NumberSetting min(int min) {
		setMinValue(min);
		set(getCurrentValue());
		return this;
	}

	/**
	 * Sets the upper limit the value can have.
	 */
	public NumberSetting max(int max) {
		setMaxValue(max);
		set(getCurrentValue());
		return this;
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		drawIcon(x, y);
	}

}
