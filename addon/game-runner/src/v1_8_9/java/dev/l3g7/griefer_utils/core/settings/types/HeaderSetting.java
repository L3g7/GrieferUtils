/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.settings.types;

import dev.l3g7.griefer_utils.core.settings.BaseSetting;
import dev.l3g7.griefer_utils.core.settings.Settings;

import java.util.Collections;
import java.util.List;

public interface HeaderSetting extends BaseSetting<HeaderSetting> {

	static HeaderSetting create(String name) {return Settings.settings.createHeaderSetting(name);}

	static HeaderSetting createText(String... rows) {return Settings.settings.createHeaderSettingWithRows(rows);}

	static HeaderSetting create() {return Settings.settings.createHeaderSetting("");}

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
	default HeaderSetting addSetting(BaseSetting<?> setting) {
		throw new UnsupportedOperationException();
	}

	@Override
	default HeaderSetting addSetting(int index, BaseSetting<?> setting) {
		throw new UnsupportedOperationException();
	}

	@Override
	default List<BaseSetting<?>> getChildSettings() {
		return Collections.emptyList();
	}

}
