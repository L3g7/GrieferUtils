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

import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.reflection.Reflection;
import dev.l3g7.griefer_utils.misc.Named;
import dev.l3g7.griefer_utils.settings.ElementBuilder;
import dev.l3g7.griefer_utils.settings.FocusableSetting;
import dev.l3g7.griefer_utils.settings.ValueHolder;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.DropDownElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.DrawUtils;
import net.labymod.utils.ModColor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static dev.l3g7.griefer_utils.core.reflection.Reflection.c;

/**
 * A setting to select enums using a dropdown.
 */
@SuppressWarnings("unchecked")
public class DropDownSetting<E extends Enum<E> & Named> extends DropDownElement<E> implements ElementBuilder<DropDownSetting<E>>, ValueHolder<DropDownSetting<E>, E>, FocusableSetting {

	private final Storage<E> storage;
	private final IconStorage iconStorage = new IconStorage();
	private final FixedDropDownMenu<E> menu = new FixedDropDownMenu<>();
	private Function<E, String> stringProvider = e -> ((Enum<?>) e).toString();

	public DropDownSetting(Class<E> enumClass) {
		this(enumClass, 0);
	}

	public DropDownSetting(Class<E> enumClass, int skippedEntries) {
		super("§cNo name set", null);
		setChangeListener(this::set);

		// Initialize storage
		storage = new Storage<>(e -> new JsonPrimitive(e.name()), s -> Enum.valueOf(enumClass, s.getAsString()), enumClass.getEnumConstants()[0]);

		// Initialize menu
		E[] constants = enumClass.getEnumConstants();
		menu.fill(Arrays.copyOfRange(constants, skippedEntries, constants.length));
		Reflection.set(this, menu, "dropDownMenu");

		// Use name field as default stringProvider
		stringProvider(e -> ((Named) e).getName());
	}

	@Override
	public void setFocused(boolean focused) {
		menu.setOpen(focused);
	}

	@Override
	public boolean isFocused() {
		return menu.isOpen();
	}

	/**
	 * A dropdown menu with overwritten setWidth and setX methods to prevent LabyMod from
	 * changing the bounds when rendering.
	 */
	private static class FixedDropDownMenu<E> extends DropDownMenu<E> {

		public FixedDropDownMenu() {
			super("", 0, 0, 0, 0);
		}

		@Override
		public void setWidth(int width) {}

		@Override
		public void setX(int x) {}

		public void doSetWidth(int width) {
			super.setWidth(width);
		}

		public void doSetX(int x) {
			super.setX(x);
		}

	}

	@Override
	public DropDownSetting<E> name(String name) {
		if (getIconData() == null)
			menu.setTitle(name);
		return ElementBuilder.super.name(name);
	}

	@Override
	public DropDownSetting<E> icon(Object icon) {
		menu.setTitle("");
		return ElementBuilder.super.icon(icon);
	}

	@Override
	public DropDownSetting<E> set(E value) {
		menu.setSelected(c(value));
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
		menu.setEntryDrawer((o, x, y, trimmedEntry) -> drawUtils.drawString(function.apply((E) o), x, y));
		int width = ((List<E>) Reflection.get(menu, "list"))
			.stream().mapToInt(e -> drawUtils.getStringWidth(function.apply(e)))
			.max()
			.orElse(0)
			+ 9;

		menu.doSetWidth(width + 15); // 15px for arrow

		return this;
	}

	@Override
	public Storage<E> getStorage() {
		return storage;
	}

	@Override
	public IconStorage getIconStorage() {
		return iconStorage;
	}

	@Override
	public void draw(int x, int y, int maxX, int maxY, int mouseX, int mouseY) {

		// Adapt dropdown width to name
		int originalWidth = menu.getWidth();
		String name = getDisplayName();
		if (name.contains(" "))
			name = Arrays.stream(name.split(" ")).max(Comparator.comparingInt(String::length)).orElse("§c");

		menu.doSetWidth(Math.min(menu.getWidth(), maxX - x - 35 - mc.fontRendererObj.getStringWidth(name))); // dropdown width - 35px for icon and padding - name

		DrawUtils drawUtils = LabyMod.getInstance().getDrawUtils();

		// Reset selection, so selected value isn't rendered
		E selected = c(menu.getSelected());
		menu.setSelected(null);
		int width = menu.getWidth();

		menu.doSetX(maxX - width - 5);
		super.draw(x, y, maxX, maxY, mouseX, mouseY);
		drawIcon(x, y);

		menu.setSelected(selected);

		int height = maxY - y - 6;

		// Draw selected entry with fixed width
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(0,0, (int) ((maxX - 21) / new ScaledResolution(mc).getScaledWidth_double() * mc.displayWidth), mc.displayHeight);

		String trimmedEntry = drawUtils.trimStringToWidth(ModColor.cl("f") + stringProvider.apply(selected), width - 10);
		drawUtils.drawString(trimmedEntry, menu.getX() + 5, (y + 3) + height / 2f - 4);

		GL11.glDisable(GL11.GL_SCISSOR_TEST);

		// Draw gradient
		drawUtils.drawGradientShadowRight(maxX - 21, y + 3, maxY - 3);
		drawUtils.drawGradientShadowRight(maxX - 21, y + 3, maxY - 3);

		// Hide overlapping pixels
		Gui.drawRect(maxX - 21, maxY - 13, maxX - 20, maxY - 3, 0xFF000000);

		// Draw dropdown with fixed width
		if (!menu.isOpen())
			return;

		menu.doSetWidth(originalWidth);
		menu.drawMenuDirect(menu.getX(), menu.getY(), mouseX, mouseY);
	}

}
