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
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.DropDownElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;

import java.util.List;
import java.util.function.Function;

import static dev.l3g7.griefer_utils.util.reflection.Reflection.c;

/**
 * A setting to select enums using a dropdown.
 */
@SuppressWarnings("unchecked")
public class DropDownSetting<E extends Enum<E>> extends DropDownElement<E> implements ElementBuilder<DropDownSetting<E>>, ValueHolder<DropDownSetting<E>, E> {

	private final Storage<E> storage;
	private Function<E, String> stringProvider = Enum::toString;
	private int dropDownWidth;

	public DropDownSetting(Class<E> enumClass) {
		super("§cNo name set", null);
		setChangeListener(this::set);

		// Initialize storage
		storage = new Storage<>(e -> new JsonPrimitive(e.name()), s -> Enum.valueOf(enumClass, s.getAsString()));

		// Initialize menu
		DropDownMenu<E> menu = new DropDownMenu<>("", 0, 0, 0, 0);
		menu.fill(enumClass.getEnumConstants());
		Reflection.set(this, menu, "dropDownMenu");

		// Use name field as default stringProvider
		stringProvider(e -> Reflection.get(e, "name"));
	}

	@Override
	public DropDownSetting<E> name(String name) {
		if (getIconData() == null)
			getDropDownMenu().setTitle(name);
		return ElementBuilder.super.name(name);
	}

	@Override
	public DropDownSetting<E> icon(Object icon) {
		getDropDownMenu().setTitle("");
		return ElementBuilder.super.icon(icon);
	}

	@Override
	public DropDownSetting<E> set(E value) {
		getDropDownMenu().setSelected(c(value));
		return ValueHolder.super.set(value);
	}

	/**
	 * Disables sub settings for DropDownSetting, as this is not implemented in rendering.
	 */
	@Override
	public DropDownSetting<E> subSettings(List<SettingsElement> settings) {
		throw new UnsupportedOperationException("unimplemented");
	}

	/**
	 * Uses given function to convert the dropdown values to strings when drawing.
	 */
	public DropDownSetting<E> stringProvider(Function<E, String> function) {
		stringProvider = function;
		DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();
		getDropDownMenu().setEntryDrawer((o, x, y, trimmedEntry) -> drawUtils.drawString(function.apply((E) o), x, y));
		dropDownWidth = ((List<E>) Reflection.get(getDropDownMenu(), "list"))
			.stream().mapToInt(e -> drawUtils.getStringWidth(function.apply(e)))
			.max()
			.orElse(0);

		dropDownWidth = Math.max(dropDownWidth, 90);

		return this;
	}

	@Override
	public Storage<E> getStorage() {
		return storage;
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {
		DropDownMenu<?> dropDownMenu = getDropDownMenu();
		DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();

		// Reset selection, so selected value isn't rendered
		Object selected = dropDownMenu.getSelected();
		dropDownMenu.setSelected(null);

		super.draw(x, y, maxX, maxY, mouseX, mouseY);

		dropDownMenu.setSelected(c(selected));

		int height = maxY - y - 6;

		// Draw selected entry with fixed width
		String trimmedEntry = drawUtils.trimStringToWidth(ModColor.cl("f") + stringProvider.apply((E) selected), 80);
		drawUtils.drawString(trimmedEntry, (maxX - 100 - 5) + 5, (y + 3) + height / 2f - 4);

		drawUtils.drawGradientShadowRight(100, 0, 100);

		// Draw dropdown with fixed width
		if (!dropDownMenu.isOpen())
			return;

		dropDownMenu.setWidth(dropDownWidth + 9);
		dropDownMenu.drawMenuDirect(dropDownMenu.getX(), dropDownMenu.getY(), mouseX, mouseY);
		dropDownMenu.setWidth(100);
	}

}
