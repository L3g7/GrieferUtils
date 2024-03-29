/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.features.modules.booster;

import dev.l3g7.griefer_utils.api.misc.Citybuild;
import dev.l3g7.griefer_utils.v1_8_9.features.modules.booster.Booster.BoosterData;
import dev.l3g7.griefer_utils.v1_8_9.misc.server.GUClient;
import dev.l3g7.griefer_utils.v1_8_9.util.MinecraftUtil;

import java.util.*;
import java.util.stream.Collectors;

class BoosterRequestHandler {

	private static long lastSendTime = 0;
	private static Citybuild currentCitybuild = Citybuild.ANY;

	static void sendBoosterData(Collection<BoosterData> boosters) {
		if (!GUClient.get().isAvailable())
			return;

		long sendTime = lastSendTime = System.currentTimeMillis();

		new Timer("GrieferUtils-Booster-Timer", true).schedule(new TimerTask() {
			public void run() {
				if (lastSendTime != sendTime)
					return;

				Map<String, List<Long>> value = new HashMap<>();

				for (BoosterData data : boosters)
					value.put(data.displayName.toLowerCase(), data.expirationDates.stream().map(l -> l / 1000).collect(Collectors.toList()));

				GUClient.get().sendBoosterData(MinecraftUtil.getCurrentCitybuild(), value);
			}
		}, 1000);
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
				booster.expirationDates.addAll(expirationDates);
			}
		}).start();
	}

	static void deleteBoosterData() {
		if (currentCitybuild == null || !GUClient.get().isAvailable())
			return;

		Citybuild citybuild = currentCitybuild;
		currentCitybuild = null;
		new Thread(() -> GUClient.get().sendBoosterData(citybuild, null)).start();
	}

}
