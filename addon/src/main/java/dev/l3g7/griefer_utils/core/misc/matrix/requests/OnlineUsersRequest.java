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

package dev.l3g7.griefer_utils.core.misc.matrix.requests;

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.misc.matrix.requests.sync.SyncRequest;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.PostRequest;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.Response;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A custom user presence route.
 * Given a list of UUIDs, this route resolves every entry by prepending a <code>minecraft-</code> and parsing it as a user localpart.
 * Every resolved user which has sent a {@link SyncRequest} within the last 10 seconds is then returned.
 */
public class OnlineUsersRequest extends PostRequest<List<UUID>> {

	@SerializedName("users_requested")
	private Set<UUID> usersRequested;

	public OnlineUsersRequest(Set<UUID> usersRequested) {
		super("/_matrix/client/v3/online_users");
		this.usersRequested = usersRequested;
	}

	@Override
	protected List<UUID> parseResponse(Session session, Response response) throws Throwable {
		return response.convertTo(OnlineUsersResponse.class).usersOnline;
	}

	private static class OnlineUsersResponse {

		@SerializedName("users_online")
		List<UUID> usersOnline;

	}

}