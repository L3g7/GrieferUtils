/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.item.recraft;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;

@Bridged
public interface RecraftBridge {

	RecraftBridge recraftBridge = FileProvider.getBridge(RecraftBridge.class);

	BaseSetting<?> getPagesSetting();

	void openPieMenu(boolean animation);

	void closePieMenu();

	RecraftRecording createEmptyRecording();

	default void init() {}

}
