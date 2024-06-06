/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.bridges;

import dev.l3g7.griefer_utils.core.api.event.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.core.api.event.event_bus.Event;
import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.GuiScreenEvent.GuiOpenEvent;
import dev.l3g7.griefer_utils.core.misc.gui.guis.ChangelogScreen;

public class EventBridge {

	private static boolean startupComplete = false;

	/**
	 * Triggers {@link OnStartupComplete} when GuiMainMenu is opened for the first time.
	 */
	@EventListener
	private static void onGuiOpen(GuiOpenEvent<?> event) {
		if (startupComplete || event.gui instanceof ChangelogScreen)
			return;

		// Call all methods annotated with @OnStartupComplete
		startupComplete = true;
		Event.fire(OnStartupComplete.class);
	}

}
