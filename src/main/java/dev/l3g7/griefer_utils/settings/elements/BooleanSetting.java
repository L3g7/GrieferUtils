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
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.utils.Consumer;

/**
 * A setting holding a boolean, represented in-game by a switch.
 */
public class BooleanSetting extends BooleanElement implements ElementBuilder<BooleanSetting>, ValueHolder<BooleanSetting, Boolean> {

	private final Storage<Boolean> storage = new ValueHolder.Storage<>(JsonPrimitive::new, JsonElement::getAsBoolean, false);
	private final IconStorage iconStorage = new IconStorage();

	public BooleanSetting() {
		super("§cNo name set", null, v -> {}, false);
		custom("An", "Aus");
		setSettingEnabled(true);
		Reflection.set(this, (Consumer<Boolean>) this::set, "toggleListener");
	}

	@Override
	public Storage<Boolean> getStorage() {
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
