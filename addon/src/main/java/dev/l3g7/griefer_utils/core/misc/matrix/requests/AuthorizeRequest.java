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
import dev.l3g7.griefer_utils.core.misc.matrix.modules.uiaa.AuthenticationMethod;
import dev.l3g7.griefer_utils.core.misc.matrix.modules.uiaa.PlayerKeyPair;
import dev.l3g7.griefer_utils.core.misc.matrix.modules.uiaa.UiaaHandler;
import dev.l3g7.griefer_utils.core.misc.matrix.types.AuthData;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.PostRequest;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.Response;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

public abstract class AuthorizeRequest extends PostRequest<AuthData> {

	@SerializedName("auth")
	public AuthenticationMethod authMethod;
	private transient final PlayerKeyPair playerKeyPair;

	protected AuthorizeRequest(String path, PlayerKeyPair playerKeyPair) {
		super(path);
		this.playerKeyPair = playerKeyPair;
	}

	@Override
	protected AuthData parseResponse(Session session, Response rawResponse) throws Throwable {
		switch (rawResponse.statusCode()) {

			case HTTP_OK:
				return rawResponse.convertTo(AuthData.class);

			case HTTP_UNAUTHORIZED:
				// Create authentication
				this.authMethod = UiaaHandler.createAuthentication(rawResponse, playerKeyPair);

				// Retry
				return send(session);

			default:
				throw new UnsupportedOperationException("Unknown response code " + rawResponse.statusCode());
		}
	}

	public static class RegisterRequest extends AuthorizeRequest {

		public final String username, password;

		@SerializedName("initial_device_display_name")
		public final String deviceDisplayName = "GrieferUtils";

		@SerializedName("refresh_token")
		public final boolean refreshToken = true;

		public RegisterRequest(String username, String password, PlayerKeyPair playerKeyPair) {
			super("/_matrix/client/v3/register", playerKeyPair);
			this.username = username;
			this.password = password;
		}

	}

	public static class LoginRequest extends AuthorizeRequest {

		@SerializedName("type")
		public final String loginMethod;

		@SerializedName("initial_device_display_name")
		public final String initialDeviceDisplayName;

		@SerializedName("identifier")
		public final UserIdentifier identifier;

		public LoginRequest(String identifier, PlayerKeyPair playerKeyPair) {
			super("/_matrix/client/v3/login", playerKeyPair);
			this.loginMethod = "dev.l3g7.minecraft_auth.auth.minecraft";
			this.initialDeviceDisplayName = null;
			this.identifier = new UserIdentifier(identifier);
		}

		private static class UserIdentifier {

			public final String type = "m.id.user";
			public final String user;

			public UserIdentifier(String user) {
				this.user = user;
			}

		}
	}
}