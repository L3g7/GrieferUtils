/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.debug;

import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;

public class EventBusBlocker {

	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("Eventbus deaktivieren")
		.description("Deaktiviert GrieferUtils' Eventbus.")
		.icon("brick")
		.callback(b -> Event.fireEvents = !DebugSettings.enabled.get() || !b);

}
