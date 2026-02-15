/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.debug;

import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import net.minecraft.init.Blocks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecraftLogger {

	private static final Logger LOGGER = LogManager.getLogger("RecraftLogger");

	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("Recraft-Logger")
		.description("Loggt Informationen über Recraft.")
		.icon("crafting_table");

	public static void log(String text) {
		if (enabled.get() && DebugSettings.enabled.get())
			LOGGER.info(text);
	}

}
