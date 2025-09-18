/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.uncategorized.debug;

import dev.l3g7.griefer_utils.core.api.event_bus.Event;
import dev.l3g7.griefer_utils.core.api.event_bus.EventProfiler;
import dev.l3g7.griefer_utils.core.settings.types.NumberSetting;
import dev.l3g7.griefer_utils.core.settings.types.StringSetting;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EventBusProfiler {

	public static final NumberSetting minTime = NumberSetting.create()
		.name("Mindestdauer")
		.description("Wie lange Events mindestens zum Auslösen brauchen, damit sie geloggt werden (in ms).")
		.icon(Blocks.barrier)
		.min(0)
		.defaultValue(5);

	private static final StringSetting filter = StringSetting.create()
		.name("Filter")
		.description("Das Event, dessen Listener gemessen werden soll.")
		.icon(Blocks.hopper);

	public static final SwitchSetting enabled = SwitchSetting.create()
		.name("Eventbus-Profiler")
		.description("Misst, wie lange das Auslösen von Events / ihrer Listener dauert.")
		.icon(Items.clock)
		.callback(EventBusProfiler::updateProfiler)
		.subSettings(minTime, filter);

	private static final Logger LOGGER = LogManager.getLogger("EventBus Profiler");
	private static final EventProfiler PROFILER = new EventProfiler() {
		@Override
		public void onEventProfile(Event event, boolean profilingListeners, long ms) {
			if (ms >= minTime.get() || profileListeners(event))
				LOGGER.info(event.getClass().getName() + " took " + ms + "ms.");
		}

		@Override
		public boolean profileListeners(Event event) {
			return filter.get().length() > 1 && event.getClass().getName().endsWith(filter.get());
		}

		@Override
		public void onListenerProfile(Object owner, long ms) {
			Class<?> clazz = owner instanceof Class<?> c ? c : owner.getClass();
			boolean isStatic = clazz == owner;
			StringBuilder sb = new StringBuilder("\t");
			if (clazz == owner)
				sb.append("Static listener ");
			else
				sb.append("Listener ");

			sb.append(clazz.getName())
				.append(" took ")
				.append(ms)
				.append("ms.");

			LOGGER.info(sb.toString());
		}
	};

	public static void updateProfiler() {
		if (!enabled.get() || !DebugSettings.enabled.get()) {
			Event.setEventProfiler(null);
			return;
		}

		Event.setEventProfiler(PROFILER);
	}

}
