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
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.PostRequest;
import dev.l3g7.griefer_utils.core.misc.matrix.types.cryptography.Curve25519Keys.SignedCurve25519Key;
import dev.l3g7.griefer_utils.core.misc.matrix.types.cryptography.DeviceKeys;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.Response;

import java.util.Map;

public class KeysUploadRequest extends PostRequest<Void> {

	// device_keys
	// fallback_keys
	// one_time_keys

	@SerializedName("device_keys")
	public DeviceKeys deviceKeys;

	@SerializedName("fallback_keys")
	public Map<String, SignedCurve25519Key> fallbackKeys;

	@SerializedName("one_time_keys")
	public Map<String, SignedCurve25519Key> oneTimeKeys;

	public KeysUploadRequest(DeviceKeys deviceKeys, Map<String, SignedCurve25519Key> fallbackKeys, Map<String, SignedCurve25519Key> oneTimeKeys) {
		super("/_matrix/client/v3/keys/upload");
		this.deviceKeys = deviceKeys;
		this.fallbackKeys = fallbackKeys;
		this.oneTimeKeys = oneTimeKeys;
	}

	@Override
	protected Void parseResponse(Session session, Response response) {
		return null;
	}

}
