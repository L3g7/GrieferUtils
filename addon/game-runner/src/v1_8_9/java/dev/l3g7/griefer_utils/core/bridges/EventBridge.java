/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.bridges;

import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.DrawScreenEvent;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnStartupComplete;

public class EventBridge {

	private static boolean startupComplete = false;

	/**
	 * Triggers {@link OnStartupComplete} when the main menu is drawn for the first time.
	 */
	@EventListener
	private static void onGuiOpen(DrawScreenEvent event) {
		if (startupComplete)
			return;

		// Call all methods annotated with @OnStartupComplete
		startupComplete = true;
		Event.fire(OnStartupComplete.class);
	}

}
