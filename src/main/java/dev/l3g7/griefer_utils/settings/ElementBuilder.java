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

package dev.l3g7.griefer_utils.settings;

import dev.l3g7.griefer_utils.settings.elements.HeaderSetting;
import dev.l3g7.griefer_utils.util.Util;
import dev.l3g7.griefer_utils.util.misc.Constants;
import dev.l3g7.griefer_utils.util.reflection.Reflection;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.ControlElement.IconData;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.CaseFormat.*;
import static dev.l3g7.griefer_utils.util.Util.elevate;

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
	 * Sets the name of the setting, joined by \n.
	 */
	default S name(String... name) {
		return name(String.join("\n", name));
	}

	/**
	 * Sets the description of the setting to the given strings.
	 */
	default S description(String... description) {
		if (description.length == 0)
			((S) this).setDescriptionText(null);
		else
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
		else if (icon instanceof ResourceLocation)
			iconData = new IconData((ResourceLocation) icon);
		else if (icon instanceof String) {
			if (((String) icon).startsWith("labymod:"))
				iconData = new IconData("labymod/textures/" + ((String) icon).substring(8) + ".png");
			else
				iconData = new IconData("griefer_utils/icons/" + icon + ".png");
		}
		else if (icon instanceof ItemStack) {
			iconData = new IconData();
			getIconStorage().itemStack = (ItemStack) icon;
		} else
			throw new UnsupportedOperationException(icon.getClass().getSimpleName() + " is an unsupported icon type!");

		Reflection.set(this, iconData, "iconData");
		return (S) this;
	}

	default S icon(Supplier<?> icon) {
		return icon(icon.get());
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
			new HeaderSetting("§e§l" + ((S) this).getDisplayName().replaceAll("§.", "")).scale(.7),
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

	default S settingsEnabled(boolean settingsEnabled) {
		if(this instanceof ControlElement) {
			((ControlElement) this).setSettingEnabled(settingsEnabled);
			return (S) this;
		}

		throw new UnsupportedOperationException(this.getClass().getSimpleName() + "doesn't support settings!");
	}

	static Pair<SettingsElement, String> initMainElement(Object owner, String parentKey) {
		Class<?> ownerClass = owner.getClass();

		// Load main element
		Field[] mainElementFields = Reflection.getAnnotatedFields(ownerClass, MainElement.class, false);
		if (mainElementFields.length != 1)
			throw new IllegalStateException("Found an invalid amount of main elements for " + ownerClass.getSimpleName());

		Field mainElementField = mainElementFields[0];
		SettingsElement mainElement = Reflection.get(owner, mainElementField);

		// Load config key
		String configKey = UPPER_CAMEL.to(LOWER_UNDERSCORE, ownerClass.getSimpleName());
		if (parentKey != null)
			configKey = parentKey + "." + configKey;

		// Load settings
		if (mainElement instanceof ValueHolder<?, ?>)
			((ValueHolder<?, ?>) mainElement).config(configKey + "." + mainElementField.getName());

		if (mainElementField.getAnnotation(MainElement.class).configureSubSettings())
			loadSubSettings(owner, mainElement, configKey);

		return Pair.of(mainElement, configKey);
	}

	/**
	 * Loads the config values for all subSettings.
	 */
	static void loadSubSettings(Object owner, SettingsElement parent, String parentKey) {
		for (SettingsElement element : new ArrayList<>(parent.getSubSettings().getElements())) {
			if (element instanceof HeaderSetting)
				continue;

			boolean hasSubSettings = !element.getSubSettings().getElements().isEmpty();
			if (!hasSubSettings && !(element instanceof ValueHolder<?, ?>))
				continue;

			Field field = Arrays.stream(Reflection.getAllFields(owner.getClass()))
				.filter(f -> Reflection.get(owner, f) == element)
				.findFirst()
				.orElseThrow(() -> elevate(new NoSuchFieldException(), "Could not find declaration field for " + element.getDisplayName() + " in " + owner));

			if (field.getName().equals("value"))
				throw elevate(new IllegalStateException(), field + " has an illegal name!");

			String key = parentKey + "." + LOWER_CAMEL.to(LOWER_UNDERSCORE, field.getName());
			loadSubSettings(owner, element, key);
			if (element instanceof ValueHolder<?, ?>) {
				try {
					if (hasSubSettings)
						((ValueHolder<?, ?>) element).config(key + ".value");
					else
						((ValueHolder<?, ?>) element).config(key);
				} catch (RuntimeException t) {
					throw Util.addMessage(t, "loading config for %s.%s failed!", field.getDeclaringClass().getSimpleName(), field.getName());
				}
			}
		}
	}

	/**
	 * An annotation for marking the main element in a feature.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface MainElement {

		boolean configureSubSettings() default true;

	}

	class IconStorage {

		private ItemStack itemStack = null;

	}
}
