/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.settings;


import dev.l3g7.griefer_utils.core.misc.tags.Tags;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;

import static dev.l3g7.griefer_utils.core.misc.tags.Tags.TagManager.tagManager;

/**
 * @see Tags
 */
public class Badges {

	public static final SwitchSetting showPercentage = SwitchSetting.create()
		.name("Nutzer-Prozentsatz anzeigen")
		.description("Zeigt über der Tabliste an, wie viel Prozent der Spieler GrieferUtils benutzen.")
		.config("settings.badges")
		.icon("icon")
		.defaultValue(true);

	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("GrieferUtils-\nNutzer-Anzeige")
		.description("""
			Zeigt vor den Namen von Spielern ein GrieferUtils-Icon an, wenn sie das Addon benutzen.

			§nFarben:§r
			§cRot§r: Offizieller Account / Entwickler
			§bBlau§r: Entwickler
			§aGrün§r: Supporter""")
		.config("settings.badges")
		.icon("icon")
		.defaultValue(true)
		.subSettings(showPercentage)
		.callback(tagManager::toggleBadges);

	public static boolean showBadges() {
		return enabled.get();
	}

}
