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

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.PostRequest;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.Response;

public class RoomCreateRequest extends PostRequest<String> {

	public final String[] invite;
	@SerializedName("is_direct")
	public boolean isDirect;
	public final String preset;
	@SerializedName("initial_state")
	public EncryptionStateEvent[] initialState = new EncryptionStateEvent[]{ new EncryptionStateEvent() };

	public RoomCreateRequest(String[] invite, boolean isDirect, String preset) {
		super("/_matrix/client/v3/createRoom");
		this.invite = invite;
		this.isDirect = isDirect;
		this.preset = preset;
	}

	@Override
	protected String parseResponse(Session session, Response response) {
		return response.convertTo(RoomCreateResponse.class).roomId;
	}

	public static class RoomCreateResponse {

		@SerializedName("room_id")
		public String roomId;

	}

	private static class EncryptionStateEvent {
		@SerializedName("state_key")
		public final String stateKey = "";
		public String type = "m.room.encryption";
		public Content content = new Content();

		private static class Content {
			public String algorithm = "m.megolm.v1.aes-sha2";
		}
	}

}