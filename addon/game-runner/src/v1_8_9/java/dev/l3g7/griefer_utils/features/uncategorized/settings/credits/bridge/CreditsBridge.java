/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.uncategorized.settings.credits.bridge;

import dev.l3g7.griefer_utils.core.api.bridges.Bridge.Bridged;
import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.core.settings.BaseSetting;

import java.util.List;

@Bridged
public interface CreditsBridge {

	CreditsBridge creditsBridge = FileProvider.getBridge(CreditsBridge.class);

	void addTeam(List<BaseSetting<?>> elements);

	BaseSetting<?> createIconSetting(String displayName, String icon);

	BaseSetting<?> createTextSetting(String... text);

	BaseSetting<?> createCookieLib();

	BaseSetting<?> createUserSetting();

}
