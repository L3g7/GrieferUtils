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
import dev.l3g7.griefer_utils.core.event_bus.EventRegisterer;
import dev.l3g7.griefer_utils.core.misc.functions.Predicate;
import dev.l3g7.griefer_utils.core.misc.functions.Supplier;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.misc.gui.guis.AddonsGuiWithCustomBackButton;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.FocusableSetting;
import dev.l3g7.griefer_utils.settings.ValueHolder;
import net.labymod.gui.elements.ModTextField;
import net.labymod.settings.elements.StringElement;
import net.labymod.utils.Consumer;

import static dev.l3g7.griefer_utils.util.MinecraftUtil.mc;
import static net.minecraft.client.gui.Gui.drawRect;

/**
 * A setting holding a string, represented in-game by a text input.
 */
public class StringSetting extends StringElement implements ElementBuilder<StringSetting>, ValueHolder<StringSetting, String>, FocusableSetting {

	private final Storage<String> storage = new Storage<>(JsonPrimitive::new, JsonElement::getAsString, "");
	private final IconStorage iconStorage = new IconStorage();

	private Predicate<String> validator = null;
	private boolean invalid = false;
	private final Supplier<Boolean> closeCheck = () -> !invalid;

	public StringSetting() {
		super("§cNo name set", null, "", v -> {});
		setSettingEnabled(true);
		Reflection.set(this, (Consumer<String>) value -> {
			if (validator != null) {
				boolean failed = !validator.test(value);
				if (invalid != failed) {
					invalid = failed;
					name(invalid ? "§c" + displayName : displayName.substring(2));
				}
			}

			if (invalid)
				return;

			getStorage().value = value;
			getStorage().callbacks.forEach(c -> c.accept(value));
		}, "changeListener");

		EventRegisterer.register(this);
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

		if (invalid) {
			int rectX = maxX - 82;
			drawRect(rectX, y, rectX + 80, y + 1, 0xFFFF5555);
			drawRect(rectX, y + 21, rectX + 80, y + 22, 0xFFFF5555);
			drawRect(rectX - 1, y, rectX, y + 22, 0xFFFF5555);
			drawRect(rectX + 80, y, rectX + 81, y + 22, 0xFFFF5555);
		}

		if (validator == null)
			return;

		if (!(mc().currentScreen instanceof AddonsGuiWithCustomBackButton))
			mc().displayGuiScreen(new AddonsGuiWithCustomBackButton(null));

		((AddonsGuiWithCustomBackButton) mc().currentScreen).addCheck(closeCheck);
	}

	@Override
	public void setFocused(boolean focused) {
		ModTextField textField = Reflection.get(this, "textField");
		textField.setCursorPositionEnd();
		textField.setFocused(focused);
	}

	@Override
	public boolean isFocused() {
		ModTextField textField = Reflection.get(this, "textField");
		return textField.isFocused();
	}

	public StringSetting setValidator(Predicate<String> validator) {
		this.validator = validator;
		return this;
	}

}
