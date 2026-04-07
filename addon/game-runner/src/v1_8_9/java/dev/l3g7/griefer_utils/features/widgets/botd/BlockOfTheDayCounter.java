/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.features.widgets.botd;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.api.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.api.file_provider.Singleton;
import dev.l3g7.griefer_utils.core.api.misc.NTP;
import dev.l3g7.griefer_utils.core.api.misc.config.Config;
import dev.l3g7.griefer_utils.core.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.core.events.griefergames.BlockOfTheDayRewardEvent;
import dev.l3g7.griefer_utils.core.events.griefergames.CitybuildJoinEvent;
import dev.l3g7.griefer_utils.core.settings.types.SwitchSetting;
import dev.l3g7.griefer_utils.core.util.MinecraftUtil;
import dev.l3g7.griefer_utils.features.Feature.MainElement;
import dev.l3g7.griefer_utils.features.widgets.Widget.SimpleWidget;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.util.MinecraftUtil.*;

@Singleton
public class BlockOfTheDayCounter extends SimpleWidget {

	private static final String PATH = "modules.block_of_the_day_counter";
	private static final Map<UUID, Integer> values = new HashMap<>();
	private static long nextReset = getNextServerRestart();

	private static final SwitchSetting showPopup = SwitchSetting.create()
		.name("Popup anzeigen")
		.description("Zeigt ein Popup an, wenn ein Block des Tages gefunden wurde.")
		.icon("bell");

	@MainElement
	private final SwitchSetting enabled = SwitchSetting.create()
		.name("Block des Tages-Zähler")
		.description("Zählt, wie oft Block des Tages gefunden wurde.")
		.icon("brick")
		.subSettings(showPopup)
		.since("2.4-BETA-1");

	@EventListener
	private void temp(CitybuildJoinEvent event) {
		onReward(null);
	}

	@EventListener
	private void onReward(BlockOfTheDayRewardEvent event) {
		checkReset();
		UUID uuid = uuid();
		values.put(uuid, values.getOrDefault(uuid, 0) + 1);
		save();
		if (!showPopup.get())
			return;

		mc().ingameGUI.displayTitle("§aBlock des Tages", null, -1, -1, -1);
		mc().ingameGUI.displayTitle(null, "§fDu hast einen §aBlock des Tages §fgefunden!", -1, -1, -1);
		mc().ingameGUI.displayTitle(null, null, 0, 50, 10);
	}

	private static void checkReset() {
		if (nextReset < NTP.getAccurateTime()) {
			nextReset = MinecraftUtil.getNextServerRestart();
			values.clear();
		}
	}

	@OnEnable
	private static void load() {
		if (Config.has(PATH + ".values")) {
			for (Entry<String, JsonElement> entry : Config.get(PATH + ".values").getAsJsonObject().entrySet())
				values.put(UUID.fromString(entry.getKey()), entry.getValue().getAsInt());
		}

		if (Config.has(PATH + ".next_reset")) {
			nextReset = Config.get(PATH + ".next_reset").getAsLong();
			checkReset();
		}
	}

	private static void save() {
		JsonObject valuesObj = new JsonObject();
		for (Entry<UUID, Integer> entry : values.entrySet())
			valuesObj.add(entry.getKey().toString(), new JsonPrimitive(entry.getValue()));

		Config.set(PATH + ".values", valuesObj);
		Config.set(PATH + ".next_reset", new JsonPrimitive(nextReset));
		Config.save();
	}

	@Override
	public String getValue() {
		return String.valueOf(values.get(uuid()));
	}

}
