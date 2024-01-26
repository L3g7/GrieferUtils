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

package dev.l3g7.griefer_utils.settings.types;

import dev.l3g7.griefer_utils.settings.BaseSetting;

import java.util.Collections;
import java.util.List;

import static dev.l3g7.griefer_utils.settings.Settings.settings;

public interface HeaderSetting extends BaseSetting<HeaderSetting> {

	static HeaderSetting create(String name) {return settings.createHeaderSetting(name);}

	static HeaderSetting createText(String... rows) {return settings.createHeaderSettingWithRows(rows);}

	static HeaderSetting create() {return settings.createHeaderSetting("");}

	default HeaderSetting scale(double scale) {
		// Unsupported in LabyMod 4
		return this;
	}

	default HeaderSetting entryHeight(int height) {
		// Unsupported in LabyMod 4
		return this;
	}

	default HeaderSetting center() {
		// Unsupported in LabyMod 3 (always centered)
		return this;
	}

	@Override
	default HeaderSetting icon(Object icon) {
		throw new UnsupportedOperationException();
	}

	@Override
	default HeaderSetting subSettings(BaseSetting<?>... settings) {
		throw new UnsupportedOperationException();
	}

	@Override
	default HeaderSetting subSettings(List<BaseSetting<?>> settings) {
		throw new UnsupportedOperationException();
	}

	@Override
	default List<BaseSetting<?>> getSubSettings() {
		return Collections.emptyList();
	}

}
