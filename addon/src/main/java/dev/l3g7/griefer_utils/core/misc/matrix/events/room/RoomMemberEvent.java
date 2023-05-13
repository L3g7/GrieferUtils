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

package dev.l3g7.griefer_utils.core.misc.matrix.events.room;

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.misc.matrix.events.Event;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Room;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;
import dev.l3g7.griefer_utils.core.misc.matrix.types.User;

import java.util.HashMap;

@Event(key = "m.room.member", asContentType = false)
public class RoomMemberEvent extends RoomEventContent {

	@SerializedName("state_key")
	public String userId;
	private EventContent content;

	@Override
	public void handle(Session session, Room room) {
		User user = User.get(session, userId);
		user.displayName = content.displayName;

		if (room.members == null)
			room.members = new HashMap<>();

		room.members.put(user, Room.MembershipState.valueOf(content.membershipState.toUpperCase()));
	}

	private static class EventContent {

		@SerializedName("displayname")
		public String displayName;

		@SerializedName("membership")
		public String membershipState;

	}
}