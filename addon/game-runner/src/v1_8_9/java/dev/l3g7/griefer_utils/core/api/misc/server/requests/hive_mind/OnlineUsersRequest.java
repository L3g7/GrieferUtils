/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.server.requests.hive_mind;

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.misc.server.Request;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static dev.l3g7.griefer_utils.core.api.misc.Constants.HIVEMIND_URL;

public class OnlineUsersRequest extends Request<List<UUID>> {

	@SerializedName("users_requested")
	private final Set<UUID> usersRequested;

	public OnlineUsersRequest(Set<UUID> usersRequested) {
		super(HIVEMIND_URL, "/online_users");
		this.usersRequested = usersRequested;
	}

	@Override
	protected List<UUID> parseResponse(Response response) {
		return response.convertTo(OnlineUsersResponse.class).usersOnline;
	}

	@Override
	public List<UUID> request(Consumer<IOException> errorHandler, boolean post) {
		try {
			// Try only once
			return request(false, post);
		} catch (IOException e) {
			// Ignore error if request failed
			return null;
		}
	}

	private static class OnlineUsersResponse {

		@SerializedName("users_online")
		List<UUID> usersOnline;

	}

}