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
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.GetRequest;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.Response;

public class WellKnownRequest extends GetRequest<String> {

	public WellKnownRequest() {
		super("/.well-known/matrix/client");
	}

	@Override
	protected String parseResponse(Session session, Response response) throws Throwable {
		if (response.statusCode() != 200)
			return session.host;

		return response.convertTo(WellKnownResponse.class).homeServer.baseUrl;
	}

	private static class WellKnownResponse {

		@SerializedName("m.homeserver")
		private HomeServer homeServer;

		private static class HomeServer {

			@SerializedName("base_url")
			private String baseUrl;

		}
	}
}