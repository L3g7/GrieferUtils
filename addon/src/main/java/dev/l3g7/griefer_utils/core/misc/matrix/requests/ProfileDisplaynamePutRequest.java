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
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.PostRequest.PutRequest;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.Response;

public class ProfileDisplaynamePutRequest extends PutRequest<Void> {

	@SerializedName("displayname")
	public String displayName;

	public ProfileDisplaynamePutRequest(String userId, String displayName) {
		super("/_matrix/client/v3/profile/" + userId + "/displayname");
		this.displayName = displayName;
	}

	@Override
	protected Void parseResponse(Session session, Response response) throws Throwable {
		return null;
	}

}