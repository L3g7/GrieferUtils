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

import java.util.HashMap;
import java.util.Map;

public class Room {

	public final String roomId;
	public transient Map<User, MembershipState> members = new HashMap<>();
	public EncryptionMetadata encryptionMetadata = new EncryptionMetadata();

	private Room(String roomId) {
		this.roomId = roomId;
	}

	public static Room get(Session session, String roomId) {
		return session.rooms.computeIfAbsent(roomId, Room::new);
	}

	public enum MembershipState {
		INVITE, JOIN, KNOCK, LEAVE, BAN
	}

	public static class EncryptionMetadata {

		public boolean encrypted;
		public int expiryTimeMs;
		public int maxMessages;

	}

}