/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby4.settings;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import dev.l3g7.griefer_utils.core.settings.types.*;
import dev.l3g7.griefer_utils.laby4.settings.types.*;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.list.EntryAddSettingImpl;
import dev.l3g7.griefer_utils.labymod.laby4.bridges.ItemBridge;
import dev.l3g7.griefer_utils.labymod.laby4.settings.types.*;
import dev.l3g7.griefer_utils.core.settings.Settings;
import dev.l3g7.griefer_utils.core.settings.types.list.EntryAddSetting;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.AbstractWidget;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.resources.ResourceLocation;

import java.util.ArrayList;

@Singleton
@Bridge
public class SettingsImpl implements Settings { // Note: replace with multiple bridges

	public static <T extends Widget> void hookChildAdd(AbstractWidget<T> w, Consumer<T> callback) {
		Reflection.set(w, "children", new ArrayList<>(w.getChildren()) {
			public void add(int index, T element) {
				super.add(index, element);
				try {
					callback.acceptWithException(element);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static Icon buildIcon(Object icon) {
		if (icon == null)
			return null;

		if (icon instanceof String)
			return Icon.texture(ResourceLocation.create("griefer_utils", "icons/" + icon + ".png"));
		else if (icon instanceof ResourceLocation location)
			return Icon.texture(location);
		else if (icon instanceof Icon)
			return (Icon) icon;
		else if (ItemBridge.itemBridge.isConvertableToLabyStack(icon))
			return new ItemStackIcon(ItemBridge.itemBridge.convertToLabyStack(icon));
		else
			throw new UnsupportedOperationException(icon.getClass().getSimpleName() + " is an unsupported icon type!");
	}

	@Override
	public CategorySetting createCategorySetting() {
		return new CategorySettingImpl();
	}

	@Override
	public HeaderSetting createHeaderSetting(String name) {
		return new HeaderSettingImpl(name);
	}

	@Override
	public HeaderSetting createHeaderSettingWithRows(String... rows) {
		return new HeaderSettingImpl(rows);
	}

	@Override
	public SwitchSetting createSwitchSetting() {
		return new SwitchSettingImpl();
	}

	@Override
	public SliderSetting createSliderSetting() {
		return new SliderSettingImpl();
	}

	@Override
	public StringSetting createStringSetting() {
		return new StringSettingImpl();
	}

	@Override
	public StringListSetting createStringListSetting() {
		return new StringListSettingImpl();
	}

	@Override
	public NumberSetting createNumberSetting() {
		return new NumberSettingImpl();
	}

	@Override
	public KeySetting createKeySetting() {
		return new KeySettingImpl();
	}

	@Override
	public ButtonSetting createButtonSetting() {
		return new ButtonSettingImpl();
	}

	@Override
	public CitybuildSetting createCitybuildSetting() {
		return new CitybuildSettingImpl();
	}

	@Override
	public <E extends Enum<E> & Named> DropDownSetting<E> createDropDownSetting(Class<E> enumClass) {
		return new DropDownSettingImpl<>(enumClass);
	}

	@Override
	public EntryAddSetting createEntryAddSetting() {
		return new EntryAddSettingImpl();
	}

}
