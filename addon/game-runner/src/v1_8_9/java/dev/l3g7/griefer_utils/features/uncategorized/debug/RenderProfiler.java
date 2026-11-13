/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.debug;

import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.api.event_bus.EventProfiler;
import dev.l3g7.griefer_utils.core.events.render.RenderWorldLastEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.mc;

public class RenderProfiler {

	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("Render-Profiler")
		.description("Misst, wie lange Listener des RenderWorldLastEvents dauern.")
		.icon("clock")
		.callback(RenderProfiler::updateProfiler);

	private static final EventProfiler profiler = new EventProfiler() {
		@Override
		public void onEventStart(Event event, boolean profilingListeners) {
			if (profilingListeners)
				mc().mcProfiler.endStartSection("griefer_utils_render_world_last");
		}

		@Override
		public void onEventEnd(Event event, boolean profilingListeners) {}

		@Override
		public boolean profileListeners(Event event) {
			return event instanceof RenderWorldLastEvent;
		}

		@Override
		public void onListenerStart(Object owner) {
			Class<?> clazz = owner instanceof Class<?> c ? c : owner.getClass();
			mc().mcProfiler.startSection(clazz.getSimpleName());
		}

		@Override
		public void onListenerEnd(Object owner) {
			mc().mcProfiler.endSection();
		}
	};

	private static void updateProfiler() {
		if (!enabled.get() || !DebugSettings.enabled.get()) {
			Event.setEventProfiler(null);
			return;
		}

		Event.setEventProfiler(profiler);
	}

}
