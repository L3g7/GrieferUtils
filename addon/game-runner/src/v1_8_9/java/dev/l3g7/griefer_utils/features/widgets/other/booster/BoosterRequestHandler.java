/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.other.booster;

import dev.l3g7.griefer_utils.core.api.misc.Citybuild;
import dev.l3g7.griefer_utils.core.api.misc.DebounceTimer;
import dev.l3g7.griefer_utils.core.misc.TPSCountdown;
import dev.l3g7.griefer_utils.core.misc.server.GUClient;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.widgets.other.booster.Booster.BoosterData;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class BoosterRequestHandler {

	private static final DebounceTimer TIMER = new DebounceTimer("Booster", 1000);

	private static Citybuild currentCitybuild = Citybuild.ANY;

	static void sendBoosterData(Collection<BoosterData> boosters) {
		if (!GUClient.get().isAvailable())
			return;

		TIMER.schedule(() -> {
			Map<String, List<Long>> value = new HashMap<>();

			long passedSeconds = (int) (System.currentTimeMillis() / 1000);
			for (BoosterData data : boosters)
				value.put(data.displayName.toLowerCase(), data.expirationDates.stream().map(c -> c.secondsRemaining() + passedSeconds).collect(Collectors.toList()));

			GUClient.get().sendBoosterData(MinecraftUtil.getCurrentCitybuild(), value);
		});
	}

	static void requestBoosterData(Citybuild citybuild, Collection<BoosterData> boosters, Runnable callback) {
		currentCitybuild = citybuild;

		if (!GUClient.get().isAvailable()) {
			callback.run();
			return;
		}

		new Thread(() -> {
			Map<String, List<Long>> data = GUClient.get().getBoosterData(citybuild, e -> callback.run());
			if (data == null) {
				callback.run();
				return;
			}

			for (BoosterData booster : boosters) {
				List<Long> expirationDates = data.get(booster.displayName.toLowerCase());
				if (expirationDates == null)
					continue;

				booster.expirationDates.clear();
				expirationDates.stream()
					.map(TPSCountdown::fromEnd)
					.forEach(booster.expirationDates::add);
			}
		}).start();
	}

	static void deleteBoosterData() {
		Citybuild citybuild = currentCitybuild;
		if (currentCitybuild == null || !GUClient.get().isAvailable())
			return;

		currentCitybuild = null;
		new Thread(() -> GUClient.get().sendBoosterData(citybuild, null)).start();
	}

}
