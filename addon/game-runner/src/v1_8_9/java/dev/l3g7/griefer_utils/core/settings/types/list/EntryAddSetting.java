/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.settings.types.list;

import dev.l3g7.griefer_utils.core.settings.AbstractSetting;
import dev.l3g7.griefer_utils.core.settings.Settings;

public interface EntryAddSetting extends AbstractSetting<EntryAddSetting, Object> {

	static EntryAddSetting create() {return Settings.settings.createEntryAddSetting();}

}
