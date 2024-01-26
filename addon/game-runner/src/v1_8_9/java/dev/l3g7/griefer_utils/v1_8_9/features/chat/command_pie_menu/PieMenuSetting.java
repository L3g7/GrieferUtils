/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.chat.command_pie_menu;

import dev.l3g7.griefer_utils.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.laby4.settings.types.CategorySettingImpl;

public abstract class PieMenuSetting extends CategorySettingImpl { // TODO: implement PieMenuSetting

	Object container;

	protected void onChange() {
		FileProvider.getSingleton(CommandPieMenu.class).save();
	}

}
