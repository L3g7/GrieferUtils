/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.settings;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.api.misc.Named;
import dev.l3g7.griefer_utils.core.settings.types.*;
import dev.l3g7.griefer_utils.core.settings.types.list.EntryAddSetting;

@Bridged
public interface Settings {

	Settings settings = FileProvider.getBridge(Settings.class);

	CategorySetting createCategorySetting();

	HeaderSetting createHeaderSetting(String name);

	HeaderSetting createHeaderSettingWithRows(String... rows);

	SwitchSetting createSwitchSetting();

	SliderSetting createSliderSetting();

	StringSetting createStringSetting();

	StringListSetting createStringListSetting();

	NumberSetting createNumberSetting();

	KeySetting createKeySetting();

	ButtonSetting createButtonSetting();

	CitybuildSetting createCitybuildSetting();

	<E extends Enum<E> & Named> DropDownSetting<E> createDropDownSetting(Class<E> enumClass);

	EntryAddSetting createEntryAddSetting();

}
