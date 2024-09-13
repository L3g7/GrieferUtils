/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.chat.command_pie_menu.laby3;

import dev.l3g7.griefer_utils.core.api.file_provider.FileProvider;
import dev.l3g7.griefer_utils.labymod.laby3.temp.TempListEntrySetting;

public abstract class PieMenuSetting extends TempListEntrySetting {

	public PieMenuSetting() {
		super(true, true, true);
	}

	protected void onChange() {
		FileProvider.getSingleton(CommandPieMenu.class).save();
	}

}
