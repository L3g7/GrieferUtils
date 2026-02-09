/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.server.requests;

import dev.l3g7.griefer_utils.core.api.misc.server.Request;
import dev.l3g7.griefer_utils.core.misc.badges.Badges.SpecialBadge;

import java.util.Map;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.STATIC_API_URL;

public class StaticApiRequest extends Request<StaticApiRequest.StaticApiData> {

	public StaticApiRequest() {
		super(STATIC_API_URL, "/v6/");
	}

	@Override
	protected StaticApiData parseResponse(Response response) {
		return response.convertTo(StaticApiData.class);
	}

	public static class StaticApiData {

		public String addonDescription;
		public Changelog changelog;
		public Map<String, GrieferInfoItem> grieferInfoItems;
		public String[] repeatingPrefixes;
		public String[] coloredFonts;
		public Map<UUID, SpecialBadge> specialBadges;
		public String[] cooldowns;

		public static class Changelog {
			public Map<String, ChangelogEntry> merged;
		}

		public static class ChangelogEntry {
			public Map<String, String[]> changelog;
			public boolean beta;
		}

		public static class GrieferInfoItem {
			public String stack;
			public int categories;
			public boolean customName;
		}

	}

}
