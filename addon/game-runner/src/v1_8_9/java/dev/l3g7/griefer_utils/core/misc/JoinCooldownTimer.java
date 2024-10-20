/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.event_bus.Priority;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;

public class JoinCooldownTimer {

	private static long joinTime = -1;

	@EventListener(priority = Priority.HIGHEST)
	private static void onJoin(CitybuildJoinEvent e) {
		joinTime = System.currentTimeMillis();
	}

	public static boolean isCooldownExpired() {
		return System.currentTimeMillis() - joinTime > 15_000;
	}

	public static float getRemainingSeconds() {
		long remainingMS = 15000 - (System.currentTimeMillis() - joinTime);
		long time = remainingMS / 100 + /* lazy way of rounding up */ 1;
		return (time / 10f);
	}

}
