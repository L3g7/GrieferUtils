/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.laby4.settings;

import dev.l3g7.griefer_utils.api.bridges.Bridge;
import dev.l3g7.griefer_utils.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.api.misc.Named;
import dev.l3g7.griefer_utils.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.api.reflection.Reflection;
import dev.l3g7.griefer_utils.laby4.settings.types.*;
import dev.l3g7.griefer_utils.laby4.settings.types.list.EntryAddSettingImpl;
import dev.l3g7.griefer_utils.settings.Settings;
import dev.l3g7.griefer_utils.settings.types.*;
import dev.l3g7.griefer_utils.settings.types.list.EntryAddSetting;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.AbstractWidget;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.resources.ResourceLocation;

import java.util.ArrayList;

import static dev.l3g7.griefer_utils.laby4.bridges.ItemBridge.itemBridge;

@Singleton
@Bridge
public class SettingsImpl implements Settings {

	public static <T extends Widget> void hookChildAdd(AbstractWidget<T> w, Consumer<T> callback) {
		Reflection.set(w, new ArrayList<>(w.getChildren()) {
			public void add(int index, T element) {
				super.add(index, element);
				try {
					callback.acceptWithException(element);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "children");
	}

	public static Icon buildIcon(Object icon) {
		if (icon instanceof String)
			return Icon.texture(ResourceLocation.create("griefer_utils", "icons/" + icon + ".png"));
		else if (icon instanceof Icon)
			return (Icon) icon;
		else if (itemBridge.isConvertableToLabyStack(icon))
			return new ItemStackIcon(itemBridge.convertToLabyStack(icon));
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
