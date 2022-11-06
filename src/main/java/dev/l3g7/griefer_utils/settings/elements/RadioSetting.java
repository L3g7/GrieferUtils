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

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.ValueHolder;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import dev.l3g7.griefer_utils.util.render.RenderUtil;
import net.labymod.gui.elements.CheckBox;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ColorPickerCheckBoxBulkElement;

import java.util.List;
import java.util.function.Function;

import static net.labymod.gui.elements.CheckBox.EnumCheckBoxValue.*;

/**
 * A setting to select enums using radio buttons.
 */
public class RadioSetting<E extends Enum<E>> extends ColorPickerCheckBoxBulkElement implements ElementBuilder<RadioSetting<E>>, ValueHolder<RadioSetting<E>, E> {

	private final Storage<E> storage;
	private final IconStorage iconStorage = new IconStorage();
	private final List<CheckBox> checkBoxes = Reflection.get(this, Reflection.getField(ColorPickerCheckBoxBulkElement.class, "checkBoxes"));
	private final Class<E> enumClass;

	public RadioSetting(Class<E> enumClass) {
		super("§cNo name set");
		this.enumClass = enumClass;

		// Initialize storage
		storage = new Storage<>(e -> new JsonPrimitive(e.name()), s -> Enum.valueOf(enumClass, s.getAsString()));

		// Reverse values
		E[] values = enumClass.getEnumConstants();

		// Add values
		for (E e : values) {
			CheckBox checkBox = new CheckBox(e.name(), DISABLED, () -> DISABLED, 0, 0, 20, 20);
			checkBox.setUpdateListener(value -> {
				if (value == DEFAULT || value == ENABLED)
					// Use dot instead of check mark
					checkBox.updateValue(INDETERMINATE);

				else if (value == DISABLED) {
					// cancel update if no checkbox is selected
					if (checkBoxes.stream().noneMatch(c -> c != checkBox && c.getValue() == INDETERMINATE))
						checkBox.updateValue(INDETERMINATE);
				}
				else {
					// unset other checkboxes
					for (CheckBox c : checkBoxes)
						if (c != checkBox)
							c.updateValue(DISABLED);

					ValueHolder.super.set(e);
				}
			});
			addCheckbox(checkBox);
		}

		// Use name field as default stringProvider
		stringProvider(e -> Reflection.get(e, "name"));
	}

	@Override
	public RadioSetting<E> set(E value) {
		checkBoxes.get(value.ordinal()).updateValue(INDETERMINATE);
		return ValueHolder.super.set(value);
	}

	/**
	 * Uses given function to name the checkboxes when drawing.
	 */
	public RadioSetting<E> stringProvider(Function<E, String> function) {
		for (int i = 0; i < checkBoxes.size(); i++)
			Reflection.set(checkBoxes.get(i), function.apply(enumClass.getEnumConstants()[i]), "title");
		return this;
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		RenderUtil.renderIconData(iconData, x, y);
		drawIcon(x, y);

		LabyMod.getInstance().getDrawUtils().drawRectangle(x, y, maxX, maxY, 0x1e505050);
		mc.fontRendererObj.drawString(displayName, x + 25, y + 9, 0xFFFFFF);

		int xOffset = 0;
		for (int i = checkBoxes.size() - 1; i >= 0; i--) {
			CheckBox checkBox = checkBoxes.get(i);
			xOffset += Math.max(mc.fontRendererObj.getStringWidth(checkBox.getTitle()) / 2, checkBox.getWidth()) + 5;
			checkBox.setX(maxX - xOffset);
			checkBox.setY(y + 5);
			checkBox.drawCheckbox(mouseX, mouseY);
		}
	}

	@Override
	public int getEntryHeight() {
		return 25;
	}

	@Override
	public Storage<E> getStorage() {
		return storage;
	}

	@Override
	public IconStorage getIconStorage() {
		return iconStorage;
	}
}
