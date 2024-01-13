/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.core.misc.server.requests;

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.misc.functions.Consumer;
import dev.l3g7.griefer_utils.core.misc.server.types.GUSession;
import dev.l3g7.griefer_utils.core.misc.server.Request;
import dev.l3g7.griefer_utils.core.misc.server.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class OnlineUsersRequest extends Request<List<UUID>> {

	@SerializedName("users_requested")
	private Set<UUID> usersRequested;

	public OnlineUsersRequest(Set<UUID> usersRequested) {
		super("/online_users");
		this.usersRequested = usersRequested;
	}

	@Override
	protected List<UUID> parseResponse(GUSession session, Response response) {
		return response.convertTo(OnlineUsersResponse.class).usersOnline;
	}

	@Override
	public List<UUID> request(GUSession session, Consumer<IOException> errorHandler, boolean post) {
		try {
			// Try only once
			return request(session, false, post);
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