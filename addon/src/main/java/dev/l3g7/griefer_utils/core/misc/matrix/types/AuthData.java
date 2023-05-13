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

package dev.l3g7.griefer_utils.core.misc.matrix.types;

import com.google.gson.annotations.SerializedName;

public class AuthData {

	@SerializedName("access_token")
	public String accessToken;

	@SerializedName("user_id")
	public String userId;

	@SerializedName("device_id")
	public String deviceId;

	public AuthData(String accessToken, String userId, String deviceId) {
		this.accessToken = accessToken;
		this.userId = userId;
		this.deviceId = deviceId;
	}

}