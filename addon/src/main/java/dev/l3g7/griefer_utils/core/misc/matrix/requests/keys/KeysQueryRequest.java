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

package dev.l3g7.griefer_utils.core.misc.matrix.requests.keys;

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.misc.matrix.types.DeviceKeys;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.PostRequest;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.Response;

import java.util.HashMap;
import java.util.Map;

public class KeysQueryRequest extends PostRequest<Map<String, Map<String, DeviceKeys>>> {

	@SerializedName("device_keys")
	private final Map<String, String[]> deviceKeys = new HashMap<>();

	public KeysQueryRequest(Iterable<String> users) {
		super("/_matrix/client/r0/keys/query");
		for (String user : users)
			deviceKeys.put(user, new String[0]);
	}

	@Override
	protected Map<String, Map<String, DeviceKeys>> parseResponse(Session session, Response response) {
		return response.convertTo(KeysQueryResponse.class).deviceKeys;
	}

	private static class KeysQueryResponse {

		@SerializedName("device_keys")
		private Map<String, Map<String, DeviceKeys>> deviceKeys; // userId -> deviceId -> DeviceKeys

	}
}