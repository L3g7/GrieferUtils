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

package dev.l3g7.griefer_utils.settings;

import dev.l3g7.griefer_utils.misc.Constants;
import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * An interface for builder-like setting creation.
 *
 * @param <S> The implementation and element class
 */
@SuppressWarnings("unchecked")
public interface ElementBuilder<S extends SettingsElement & ElementBuilder<S>> {

	/**
	 * Sets the name of the setting.
	 */
	default S name(String name) {
		((S) this).setDisplayName(name);
		return (S) this;
	}

	/**
	 * Sets the description of the setting to the given strings.
	 */
	default S description(String... description) {
		((S) this).setDescriptionText(String.join("\n", description));
		return (S) this;
	}

	/**
	 * Sets the icon of the setting.
	 * @param icon of type {@link Material}, {@link ItemStack} or {@link String} for GrieferUtils icons.
	 */
	default S icon(Object icon) {
		if (!(this instanceof ControlElement))
			throw new UnsupportedOperationException(this.getClass().getSimpleName() + "doesn't support icons!");

		IconData iconData;
		if (icon instanceof Material)
			iconData = new IconData((Material) icon);
		else if (icon instanceof String)
			iconData = new IconData("griefer_utils/icons/" + icon + ".png");
		else if (icon instanceof ItemStack) {
			iconData = new IconData();
			getIconStorage().itemStack = (ItemStack) icon;
		} else
			throw new UnsupportedOperationException(icon.getClass().getSimpleName() + " is an unsupported icon type!");

		Reflection.set(this, iconData, "iconData");
		return (S) this;
	}

	/**
	 * A storage for ItemStack icons.
	 * Should be implemented for every setting extending {@link ControlElement}.
	 */
	default IconStorage getIconStorage() {
		throw new UnsupportedOperationException("unimplemented");
	}

	/**
	 * Draws the ItemStack icon, if set.
	 */
	default void drawIcon(int x, int y) {
		ItemStack itemIcon = getIconStorage().itemStack;
		if (itemIcon == null)
			return;

		LabyMod.getInstance().getDrawUtils().drawItem(itemIcon, x + 3, y + 2, null);
	}

	/**
	 * Sets the given settings as sub settings, with the display name as header.
	 */
	default S subSettings(SettingsElement... settings) {
		((S) this).getSubSettings().getElements().clear();
		subSettings(Arrays.asList(
			new HeaderSetting("§r"),
			new HeaderSetting("§r§e§l" + Constants.ADDON_NAME).scale(1.3),
			new HeaderSetting("§e§l" + ((S) this).getDisplayName()).scale(.7),
			new HeaderSetting("§r").scale(.4).entryHeight(10)
		));
		return subSettings(Arrays.asList(settings));
	}

	/**
	 * Adds the given settings as sub settings.
	 */
	default S subSettings(List<SettingsElement> settings) {
		((S) this).getSubSettings().getElements().addAll(settings);
		return (S) this;
	}

	class IconStorage {

		private ItemStack itemStack = null;

	}
}
