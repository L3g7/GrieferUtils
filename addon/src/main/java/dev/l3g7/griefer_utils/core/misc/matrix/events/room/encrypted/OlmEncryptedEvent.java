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
import dev.l3g7.griefer_utils.core.misc.matrix.MatrixUtil;
import dev.l3g7.griefer_utils.core.misc.matrix.events.Event;
import dev.l3g7.griefer_utils.core.misc.matrix.events.Event.EventContent;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.structures.OlmSession;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Ed25519Key;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;

import java.util.HashMap;
import java.util.Map;

/**
 * A user specific event, encrypted using Olm.
 */
public class OlmEncryptedEvent extends EventContent {

	public String algorithm;
	public Map<String, OlmMessage> ciphertext = new HashMap<>();

	@SerializedName("sender_key")
	public String senderKey;

	public OlmEncryptedEvent(Session session, String identityKey, OlmSession olmSession, EventContent content, String recipientDeviceKey, String recipientUserId) {
		algorithm = "m.olm.v1.curve25519-aes-sha2";
		senderKey = session.olmAccount.getIdentityKeys().curve25519;

		int type = olmSession.getNextMessageType();
		String ciphertext = olmSession.encrypt(MatrixUtil.GSON.toJson(new OlmEncryptionPayload(
			content.getClass().getAnnotation(Event.class).key(), content,
			session.userId, session.olmAccount.getIdentityKeys().ed25519,
			recipientDeviceKey, recipientUserId
		)));

		this.ciphertext.put(identityKey, new OlmMessage(type, ciphertext));
	}

	private static class OlmEncryptionPayload {

		@SerializedName("recipient_keys")
		public Ed25519Key recipientKey;
		public String recipient;
		public Ed25519Key keys;
		public String sender;
		public String type;
		public EventContent content;

		private OlmEncryptionPayload(String type, EventContent content, String senderId, String senderKey, String recipientKey, String recipientId) {
			this.type = type;
			this.content = content;
			this.sender = senderId;
			this.recipient = recipientId;
			this.recipientKey = new Ed25519Key(recipientKey);
			this.keys = new Ed25519Key(senderKey);
		}

	}

	private static class OlmMessage {

		public int type;
		@SerializedName("body")
		public String ciphertext;

		public OlmMessage(int type, String ciphertext) {
			this.type = type;
			this.ciphertext = ciphertext;
		}

	}

	@Override
	public void handle(Session session) {}
}
