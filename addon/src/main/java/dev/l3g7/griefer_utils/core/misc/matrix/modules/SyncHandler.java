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

package dev.l3g7.griefer_utils.core.misc.matrix.modules;

import dev.l3g7.griefer_utils.core.misc.matrix.events.Event.EventContent;
import dev.l3g7.griefer_utils.core.misc.matrix.events.Event.EventRegistry;
import dev.l3g7.griefer_utils.core.misc.matrix.events.room.RoomEventContent;
import dev.l3g7.griefer_utils.core.misc.matrix.requests.room.RoomJoinRequest;
import dev.l3g7.griefer_utils.core.misc.matrix.requests.sync.SyncRequest;
import dev.l3g7.griefer_utils.core.misc.matrix.requests.sync.SyncResponse;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Room;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;

import java.util.concurrent.CompletableFuture;

public class SyncHandler {

	public static CompletableFuture<String> sync(Session session, String nextBatch) {
		return new SyncRequest(nextBatch == null ? 5000 : 30000, nextBatch).sendAsync(session).thenApply(res -> {
			handle(session, res);
			session.save();
			System.out.println("Saved!");
			return res.nextBatch;
		});
	}

	public static void handle(Session session, SyncResponse res) {
		// Handle account data
		for (SyncResponse.RawEvent event : res.accountData.events) {
			EventContent content = EventRegistry.getContent(event);
			if (content == null)
				continue;

			content.handle(session);
		}

		// Handle one time keys
		int existingOneTimeKeys = res.deviceOneTimeKeysCount.getOrDefault("signed_curve25519", 0);
		int maxOneTimeKeys = session.olmAccount.getMaxOneTimeKeys();

		if (maxOneTimeKeys > existingOneTimeKeys) // TODO unreliable, initial sync often says 0 | wait until initial sync is over?
			session.uploadOneTimeKeys(maxOneTimeKeys - existingOneTimeKeys);

		// Handle rooms
		res.rooms.join.forEach((roomId, joinedRoom) -> {
			Room room = Room.get(session, roomId);
			session.rooms.put(roomId, room);

			handleRoomEvent(joinedRoom.timeline, session, room);
			handleRoomEvent(joinedRoom.accountData, session, room);
		});

		// Handle invitations
		res.rooms.invite.forEach((roomId, invitedRoom) -> {
			Room room = Room.get(session, roomId);

			handleRoomEvent(invitedRoom.inviteState, session, room);

			new RoomJoinRequest(roomId).sendAsync(session);
		});
	}

	private static void handleRoomEvent(SyncResponse.EventList events, Session session, Room room) {
		for (SyncResponse.RawEvent event : events.events) {
			EventContent content = EventRegistry.getContent(event);
			if (content == null)
				return;

			if (content instanceof RoomEventContent)
				((RoomEventContent) content).handle(session, room);
		}
	}
}