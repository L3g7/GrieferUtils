/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.settings.types.player_list;

import dev.l3g7.griefer_utils.core.settings.AbstractSetting;

import java.util.List;

import static dev.l3g7.griefer_utils.core.settings.Settings.settings;

public interface PlayerListSetting extends AbstractSetting<PlayerListSetting, List<PlayerListEntry>> {

	static PlayerListSetting create() {return settings.createPlayerListSetting();}

}
