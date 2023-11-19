/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2023 L3g7
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.l3g7.griefer_utils.misc.server;

import com.google.gson.Gson;
import dev.l3g7.griefer_utils.core.event_bus.EventListener;
import dev.l3g7.griefer_utils.core.util.IOUtil;
import dev.l3g7.griefer_utils.event.events.annotation_events.OnEnable;
import dev.l3g7.griefer_utils.event.events.network.ServerEvent.ServerJoinEvent;
import dev.l3g7.griefer_utils.event.events.network.WebDataReceiveEvent;
import dev.l3g7.griefer_utils.misc.badges.GrieferUtilsGroup;

import java.util.Map;
import java.util.UUID;

public class WebAPI {

	private static final Gson GSON = new Gson();
	private static Data data = null;

	@OnEnable
	private static void onEnable() {
		IOUtil.read("https://grieferutils.l3g7.dev/v3/").asJsonObject(object -> {
			data = GSON.fromJson(object, Data.class);
			new WebDataReceiveEvent(data).fire();
		});
	}

	@EventListener
	private static void onServerJoin(ServerJoinEvent event) {
		if (data == null)
			onEnable();
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

	}

}
