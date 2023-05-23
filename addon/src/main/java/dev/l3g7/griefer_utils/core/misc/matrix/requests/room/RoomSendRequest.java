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

package dev.l3g7.griefer_utils.core.misc.matrix.requests.room;

import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.PostRequest.PutRequest;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.Response;
import dev.l3g7.griefer_utils.core.util.IOUtil;

public class RoomSendRequest extends PutRequest<Void> {

	private final Object payload;

	public RoomSendRequest(String roomId, Object payload, String eventType) {
		super("/_matrix/client/v3/rooms/" + roomId + "/send/" + eventType + "/" + newTxId());
		this.payload = payload;
	}

	@Override
	public String serialize() {
		return IOUtil.gson.toJson(payload);
	}

	@Override
	protected Void parseResponse(Session session, Response response) {
		return null;
	}
}