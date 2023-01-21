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

import java.util.function.Consumer;

import static dev.l3g7.griefer_utils.settings.elements.TriggerModeSetting.TriggerMode.TOGGLE;

public class TriggerModeSetting extends DropDownSetting<TriggerModeSetting.TriggerMode> {

	public TriggerModeSetting() {
		super(TriggerMode.class);
		name("Auslösung");
		icon("lightning");
		defaultValue(TOGGLE);
	}

	@Override
	public TriggerModeSetting callback(Consumer<TriggerMode> callback) {
		return (TriggerModeSetting) super.callback(callback);
	}

	@Override
	public TriggerModeSetting defaultValue(TriggerMode value) {
		return ((TriggerModeSetting) super.defaultValue(value));
	}

	public enum TriggerMode {

		HOLD("Halten"), TOGGLE("Umschalten");

		final String name;

		TriggerMode(String name) {
			this.name = name;
		}

	}

}
