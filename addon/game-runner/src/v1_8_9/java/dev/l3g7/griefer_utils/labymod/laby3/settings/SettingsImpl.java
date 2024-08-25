/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.labymod.laby3.settings;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge;
import dev.l3g7.griefer_utils.core.api.bridges.Bridge.ExclusiveTo;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.labymod.laby3.settings.types.*;
import dev.l3g7.griefer_utils.core.settings.Settings;
import dev.l3g7.griefer_utils.core.settings.types.*;
import dev.l3g7.griefer_utils.core.settings.types.list.EntryAddSetting;

import static dev.l3g7.griefer_utils.core.api.bridges.Bridge.Version.LABY_3;

@Bridge
@Singleton
@ExclusiveTo(LABY_3)
public class SettingsImpl implements Settings {

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
