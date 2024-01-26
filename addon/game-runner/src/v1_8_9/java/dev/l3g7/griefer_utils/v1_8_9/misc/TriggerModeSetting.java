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

package dev.l3g7.griefer_utils.v1_8_9.misc;

import dev.l3g7.griefer_utils.api.misc.Named;
import dev.l3g7.griefer_utils.settings.types.DropDownSetting;

import static dev.l3g7.griefer_utils.v1_8_9.misc.TriggerModeSetting.TriggerMode.TOGGLE;

public class TriggerModeSetting {

	public static DropDownSetting<TriggerMode> create() {
		return DropDownSetting.create(TriggerMode.class)
			.name("Auslösung")
			.icon("lightning")
			.defaultValue(TOGGLE);
	}

	public enum TriggerMode implements Named {

		HOLD("Halten"), TOGGLE("Umschalten");

		final String name;

		TriggerMode(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}


	}

}
