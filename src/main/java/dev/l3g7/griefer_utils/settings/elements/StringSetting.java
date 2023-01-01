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

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.ValueHolder;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.settings.elements.StringElement;
import net.labymod.utils.Consumer;

/**
 * A setting holding a string, represented in-game by a text input.
 */
public class StringSetting extends StringElement implements ElementBuilder<StringSetting>, ValueHolder<StringSetting, String> {

	private final Storage<String> storage = new Storage<>(JsonPrimitive::new, JsonElement::getAsString, "");
	private final IconStorage iconStorage = new IconStorage();

	public StringSetting() {
		super("§cNo name set", null, "", v -> {});
		setSettingEnabled(true);
		Reflection.set(this, (Consumer<String>) value -> {
			getStorage().value = value;
			getStorage().callbacks.forEach(c -> c.accept(value));
		}, "changeListener");
	}

	@Override
	public Storage<String> getStorage() {
		return storage;
	}

	@Override
	public IconStorage getIconStorage() {
		return iconStorage;
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		drawIcon(x, y);
	}

}
