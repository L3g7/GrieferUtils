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
import dev.l3g7.griefer_utils.core.misc.matrix.types.Curve25519Keys.SignedCurve25519Key;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.PostRequest;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.Request;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.Response;

import java.util.Map;

public class KeysClaimRequest extends PostRequest<Map<String, Map<String, Map<String, SignedCurve25519Key>>>> {

	@SerializedName("one_time_keys")
	public Map<String, Map<String, String>> oneTimeKeys;
	public int timeout = 10_000;

	public KeysClaimRequest(Map<String, Map<String, String>> oneTimeKeys) {
		super("/_matrix/client/v3/keys/claim");
		this.oneTimeKeys = oneTimeKeys;
	}

	@Override
	protected Map<String, Map<String, Map<String, SignedCurve25519Key>>> parseResponse(Session session, Response response) {
		return response.convertTo(KeysClaimResponse.class).oneTimeKeys;
	}

	public static class KeysClaimResponse {

		@SerializedName("one_time_keys")
		public Map<String, Map<String, Map<String, SignedCurve25519Key>>> oneTimeKeys;

	}

}