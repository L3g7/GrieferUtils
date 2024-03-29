/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.api.misc.server.requests.hive_mind;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.api.misc.Citybuild;
import dev.l3g7.griefer_utils.api.misc.server.Request;
import dev.l3g7.griefer_utils.api.misc.server.Response;
import dev.l3g7.griefer_utils.api.misc.server.types.GUSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoosterRequest extends Request<Map<String, List<Long>>> {

	private String citybuild;
	private Map<String, List<Long>> value;
	@SerializedName("max_avg_dev_to_prev")
	private Long maxAvgDevToPrev;

	public BoosterRequest(String citybuild, Map<String, List<Long>> value) {
		super("/hive_mind/booster");

		this.citybuild = citybuild;
		this.value = value;
	}

	public BoosterRequest(Citybuild citybuild, Long maxAvgDevToPrev) {
		super("/hive_mind/booster");

		this.citybuild = citybuild.getInternalName();
		this.maxAvgDevToPrev = maxAvgDevToPrev;
	}

	@Override
	protected Map<String, List<Long>> parseResponse(GUSession session, Response response) {
		// Using Gson causes the expiration dates to be doubles, so manual parsing is required instead
		JsonObject json = response.convertTo(JsonObject.class);
		if (json == null || !json.has("known") || !json.get("known").getAsBoolean())
			return null;

		Map<String, List<Long>> data = new HashMap<>();

		for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
			if (entry.getKey().equals("known"))
				continue;

			List<Long> expirationDates = new ArrayList<>();

			JsonArray array = entry.getValue().getAsJsonArray();
			for (JsonElement value : array)
				expirationDates.add(value.getAsLong() * 1000);

			data.put(entry.getKey(), expirationDates);
		}

		return data;
	}

}
