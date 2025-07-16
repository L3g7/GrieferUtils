/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.server.requests;

import dev.l3g7.griefer_utils.core.api.misc.server.Request;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.DYNAMIC_API_URL;

public class LeaderboardRequest extends Request<LeaderboardRequest.LeaderboardData> {

	private final boolean flown;

	public LeaderboardRequest(boolean flown) {
		super(DYNAMIC_API_URL, "/leaderboard");
		this.flown = flown;
	}

	@Override
	protected LeaderboardData parseResponse(Response response) {
		return response.convertTo(LeaderboardData.class);
	}

	public static class LeaderboardData {

		public int score;
		public int position;
		public UserData next;
		public UserData previous;

	}

	public static class UserData {

		public String uuid;
		public int score;

	}

}
