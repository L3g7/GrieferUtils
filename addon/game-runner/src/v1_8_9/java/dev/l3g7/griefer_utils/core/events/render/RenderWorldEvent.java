/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.events.render;

import dev.l3g7.griefer_utils.core.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.TickEvent;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

/**
 * A RenderTickEvent called when no gui is opened.
 */
public class RenderWorldEvent extends TickEvent.RenderTickEvent {

	public RenderWorldEvent(float renderTickTime) {
		super(renderTickTime);
	}

	@EventListener
	private static void onRenderTick(RenderTickEvent event) {
		if (mc().currentScreen == null)
			new RenderWorldEvent(event.renderTickTime).fire();
	}

}
