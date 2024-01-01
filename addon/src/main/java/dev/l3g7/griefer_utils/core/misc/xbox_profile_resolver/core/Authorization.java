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

package dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.tokens.OAuth2Token;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.tokens.XToken;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.util.DateTime;
import dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.util.Requests;

import java.io.IOException;
import java.util.Map;

import static dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.core.XboxProfileResolver.GSON;
import static dev.l3g7.griefer_utils.core.misc.xbox_profile_resolver.util.Util.strMap;

public class Authorization {

	private static Authorization currentAuthorization = null;

	private OAuth2Token oauth2Token;
	private XToken userToken;
	private XToken xstsToken;

	public Authorization(String refreshToken) {
		this(new OAuth2Token(0, null, refreshToken, "00000000402b5328", new DateTime(0)), null, null);
	}

	public Authorization(OAuth2Token oauth2Token, XToken userToken, XToken xstsToken) {
		this.oauth2Token = oauth2Token;
		this.userToken = userToken;
		this.xstsToken = xstsToken;
	}

	public static void set(Authorization currentAuthorization) {
		Authorization.currentAuthorization = currentAuthorization;
	}

	public static Authorization get() {
		return currentAuthorization;
	}

	public static String getAuthorizationHeader() {
		if (!currentAuthorization.validate())
			throw new IllegalStateException("Not authorized");

		XToken xstsToken = currentAuthorization.xstsToken;
		return "XBL3.0 x=" + xstsToken.getUHS() + ";" + xstsToken.token;
	}

	public boolean validate() {
		try {
			if (xstsToken == null || !xstsToken.isValid()) {
				if (userToken == null || !userToken.isValid()) {
					if (oauth2Token == null)
						return false;
					if (!oauth2Token.isValid())
						refreshOAuth2Token();
					requestUserToken();
				}
				requestXstsToken();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void refreshOAuth2Token() throws IOException {
		// POST
		Map<String, String> data = strMap(
				"grant_type", "refresh_token",
				"scope", "service::user.auth.xboxlive.com::MBI_SSL",
				"refresh_token", oauth2Token.refreshToken,
				"client_id", oauth2Token.msaClientId
		);

		OAuth2Token newToken = GSON.fromJson(Requests.post("https://login.live.com/oauth20_token.srf", strMap(), data), OAuth2Token.class);
		if (newToken.issued == null)
			newToken.issued = new DateTime();

		oauth2Token = newToken;
	}

	private void requestUserToken() throws IOException {
		String url = "https://user.auth.xboxlive.com/user/authenticate";
		Map<String, String> headers = strMap("x-xbl-contract-version", "1", "Content-Type", "application/json", "Accept", "application/json");
		JsonObject data = new JsonObject();
		data.addProperty("RelyingParty", "http://auth.xboxlive.com");
		data.addProperty("TokenType", "JWT");
		JsonObject properties = new JsonObject();
		properties.addProperty("AuthMethod", "RPS");
		properties.addProperty("SiteName", "user.auth.xboxlive.com");
		properties.addProperty("RpsTicket", "" + oauth2Token.accessToken);
		data.add("Properties", properties);
		userToken = GSON.fromJson(Requests.post(url, headers, data), XToken.class);
	}

	private void requestXstsToken() throws IOException {
		String url = "https://xsts.auth.xboxlive.com/xsts/authorize";
		Map<String, String> headers = strMap("x-xbl-contract-version", "1");
		JsonObject data = new JsonObject();
		data.addProperty("RelyingParty", "http://xboxlive.com");
		data.addProperty("TokenType", "JWT");
		JsonObject properties = new JsonObject();
		JsonArray userTokens = new JsonArray();
		userTokens.add(new JsonPrimitive(userToken.token));
		properties.add("UserTokens", userTokens);
		properties.addProperty("SandboxId", "RETAIL");
		data.add("Properties", properties);
		xstsToken = GSON.fromJson(Requests.post(url, headers, data), XToken.class);
	}

}
