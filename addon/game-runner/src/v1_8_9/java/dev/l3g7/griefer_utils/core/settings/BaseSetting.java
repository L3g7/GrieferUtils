/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.settings;

import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface BaseSetting<S extends BaseSetting<S>> {

	Object NULL = new Object();

	String name();

	/**
	 * Sets the name of the setting.
	 */
	S name(String name);

	/**
	 * Sets the name of the setting, joined by \n.
	 */
	default S name(String... name) {
		return name(String.join("\n", name));
	}

	/**
	 * Sets the description of the setting to the given strings.
	 */
	S description(String... description);

	/**
	 * Sets the icon of the setting to a texture.
	 */
	S icon(String icon);

	/**
	 * Sets the icon of the setting to an item stack.
	 */
	S icon(ItemStack icon);

	/**
	 * Sets the given settings as sub settings, with the display name as header.
	 */
	S subSettings(BaseSetting<?>... settings);

	/**
	 * Adds the given settings as sub settings.
	 */
	S subSettings(List<BaseSetting<?>> settings);

	/**
	 * Adds the given setting as sub setting.
	 */
	S addSetting(BaseSetting<?> setting);

	S addSetting(int index, BaseSetting<?> setting);

	default void setParent(BaseSetting<?> parent) {}

	List<BaseSetting<?>> getChildSettings();

	void create(BaseSetting<?> parent);

	default <T> T into() {
		return Reflection.c(this);
	}

}