/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api;

import com.google.gson.Gson;
import dev.l3g7.griefer_utils.core.api.event.annotation_events.OnStartupComplete;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;
import dev.l3g7.griefer_utils.core.events.WebDataReceiveEvent;

import java.util.Map;
import java.util.UUID;

public class WebAPI {

	private static final Gson GSON = new Gson();
	private static Data data = null;

	@OnStartupComplete
	public static void update() {
		if (data != null)
			return;

		IOUtil.read("https://grieferutils.l3g7.dev/v4").asJsonObject(object -> {
			data = GSON.fromJson(object, Data.class);
			new WebDataReceiveEvent(data).fire();
		});
	}

	public static class Data {

		public String addonDescription;
		public Changelog changelog;
		public Map<String, GrieferInfoItem> grieferInfoItems;
		public String[] repeatingPrefixes;
		public Map<UUID, SpecialBadge> specialBadges;

		public static class Changelog {
			public Map<String, String> all;
			public String beta;
		}

		public static class GrieferInfoItem {
			public String stack;
			public int categories;
			public boolean customName;
		}

		public static class SpecialBadge {

			public String title;
			public int colorWithLabymod;
			public int colorWithoutLabymod;

		}

	}

}
