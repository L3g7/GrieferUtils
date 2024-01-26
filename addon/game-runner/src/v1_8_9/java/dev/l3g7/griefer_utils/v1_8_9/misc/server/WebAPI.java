/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.v1_8_9.misc.server;

import com.google.gson.Gson;
import dev.l3g7.griefer_utils.api.event.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.api.event.event_bus.EventListener;
import dev.l3g7.griefer_utils.api.util.IOUtil;
import dev.l3g7.griefer_utils.v1_8_9.events.network.ServerEvent.ServerJoinEvent;
import dev.l3g7.griefer_utils.v1_8_9.events.network.WebDataReceiveEvent;
import org.spongepowered.include.com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.UUID;

public class WebAPI {

	private static final Gson GSON = new Gson();
	private static Data data = null;

	@OnStartupComplete
	private static void onStartupComplete() {
		IOUtil.read("https://grieferutils.l3g7.dev/v3").asJsonObject(object -> {
			data = GSON.fromJson(object, Data.class);
			new WebDataReceiveEvent(data).fire();
		});
	}

	@EventListener
	private static void onServerJoin(ServerJoinEvent event) {
		if (data == null)
			onStartupComplete();
	}

	public static class Data {

		public String addonDescription;
		public Changelog changelog;
		public Map<String, GrieferInfoItem> grieferInfoItems;
		public String[] repeatingPrefixes;
		public Map<UUID, GrieferUtilsGroup> specialBadges;

		public static class Changelog {
			public Map<String, String> all;
			public String beta;
		}

		public static class GrieferInfoItem {
			public String stack;
			public int categories;
			public boolean custom_name;
		}

		public static class GrieferUtilsGroup {

			@SerializedName("color_with_labymod")
			public int colorWithLabymod;

			@SerializedName("color_without_labymod")
			public int colorWithoutLabymod;
			public String title;

		}

	}

}
