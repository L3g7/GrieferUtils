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

package dev.l3g7.griefer_utils.core.misc.matrix.events.room.encrypted;

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.misc.matrix.events.Event;
import dev.l3g7.griefer_utils.core.misc.matrix.events.room.RoomEventContent;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.structures.OlmOutboundGroupSession;
import dev.l3g7.griefer_utils.core.util.IOUtil;

/**
 * A room event, encrypted using MegOlm.
 */
public class MegOlmEncryptedEvent {

	public String algorithm;

	@SerializedName("sender_key")
	public String senderKey;

	public String ciphertext;

	@SerializedName("device_id")
	public String deviceId;

	@SerializedName("session_id")
	public String sessionId;

	public void createCiphertext(OlmOutboundGroupSession megolmSession, String roomId, RoomEventContent content) {
		String eventType = content.getClass().getAnnotation(Event.class).key();
		MegolmEncryptionPayload payload = new MegolmEncryptionPayload(eventType, content, roomId);
		ciphertext = megolmSession.encrypt(IOUtil.gson.toJson(payload));
	}

	private static class MegolmEncryptionPayload {

		public String type;
		public Object content;

		@SerializedName("room_id")
		public String roomId;

		public MegolmEncryptionPayload(String type, Object content, String roomId) {
			this.type = type;
			this.content = content;
			this.roomId = roomId;
		}

	}
}