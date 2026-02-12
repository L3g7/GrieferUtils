/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.botd;

import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.events.network.ServerEvent.GrieferGamesJoinEvent;

class RewardCounter {

	public static int counter = 1;

	public static boolean shouldSend() {
		return counter != -1;
	}

	public static int getCounter(Reward.RewardType type) {
		if (type != Reward.RewardType.CUSTOM && !BlockOfTheDayHandler.isEvent) {
			counter = -1;
			return type.defaultAmount;
		}

		return counter++;
	}

	@EventListener
	private static void onGGJoin(GrieferGamesJoinEvent e) {
		counter = 1;
	}

	// TODO: Config?

}
