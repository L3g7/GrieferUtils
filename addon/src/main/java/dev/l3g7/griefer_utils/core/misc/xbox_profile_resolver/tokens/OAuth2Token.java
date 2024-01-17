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

package dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.tokens;

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.util.DateTime;

import static java.util.concurrent.TimeUnit.SECONDS;

public class OAuth2Token {

	@SerializedName("expires_in")
	public long expiresIn;
	@SerializedName("access_token")
	public String accessToken;
	@SerializedName("refresh_token")
	public String refreshToken;
	public String msaClientId;
	public DateTime issued;

	public OAuth2Token(long expiresIn, String accessToken, String refreshToken, String msaClientId, DateTime issued) {
		this.expiresIn = expiresIn;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.msaClientId = msaClientId;
		this.issued = issued;
	}

	public boolean isValid() {
		return issued.add(expiresIn, SECONDS).after(DateTime.now());
	}

}
