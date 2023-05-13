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

package dev.l3g7.griefer_utils.core.misc.matrix.jna.structures;

import com.sun.jna.Memory;
import com.sun.jna.PointerType;
import dev.l3g7.griefer_utils.core.misc.matrix.MatrixUtil;
import dev.l3g7.griefer_utils.core.misc.matrix.events.Event.EventContent;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.LibOlm;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.util.Buffer;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.util.JNAUtil;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.util.size_t;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;

import java.nio.charset.StandardCharsets;

import static dev.l3g7.griefer_utils.core.misc.matrix.jna.LibOlm.LIB_OLM;
import static dev.l3g7.griefer_utils.core.misc.matrix.jna.util.JNAUtil.malloc;
import static java.nio.charset.StandardCharsets.UTF_8;

public class OlmSession extends PointerType {

	public String encryptionKeyId = null;

	/**
	 * Has no references once set, only used to prevent the Memory object from being garbage
	 * -collected and freeing the underlying memory.
	 */
	@SuppressWarnings("unused")
	private Memory allocatedMemory;

	private static OlmSession allocateSession() {
		// Allocate memory
		size_t sessionSize = LIB_OLM.olm_session_size();
		Memory sessionBuffer = malloc(sessionSize);

		// Initialize session
		OlmSession session = LIB_OLM.olm_session(sessionBuffer);
		session.allocatedMemory = sessionBuffer;
		return session;
	}

	public static OlmSession createOutbound(OlmAccount account, String theirIdentityKey, String theirOneTimeKey) {
		OlmSession session = allocateSession();

		// Create random data and allocate parameters
		size_t randomLength = LIB_OLM.olm_create_outbound_session_random_length(session);
		Buffer randomBuffer = JNAUtil.random(randomLength);
		Buffer identityKeyBuffer = malloc(theirIdentityKey.getBytes(UTF_8));
		Buffer oneTimeKeyBuffer = malloc(theirOneTimeKey.getBytes(UTF_8));

		// Create session
		size_t errorCode = LIB_OLM.olm_create_outbound_session(session, account, identityKeyBuffer, identityKeyBuffer.size(), oneTimeKeyBuffer, oneTimeKeyBuffer.size(), randomBuffer, randomLength);
		if (errorCode.equals(LIB_OLM.olm_error()))
			throw new LibOlm.OlmInvokationException("olm_create_outbound_session", LIB_OLM.olm_session_last_error(session));

		session.encryptionKeyId = account.encryptionKeyId;
		return session;
	}

	public static OlmSession deserialize(String encryptionKeyId, String data) {
		OlmSession session = allocateSession();
		String key = MatrixUtil.ENCRYPTION_KEYS.get(encryptionKeyId);

		// Allocate memory
		Buffer keyBuffer = malloc(key.getBytes(UTF_8));
		Buffer dataBuffer = malloc(data.getBytes(UTF_8));

		// Deserialize session
		size_t errorCode = LIB_OLM.olm_unpickle_session(session, keyBuffer, keyBuffer.size(), dataBuffer, dataBuffer.size());
		if (errorCode.equals(LIB_OLM.olm_error()))
			throw new LibOlm.OlmInvokationException("olm_unpickle_session", LIB_OLM.olm_session_last_error(session));

		session.encryptionKeyId = encryptionKeyId;
		return session;
	}

	public String serialize() {
		String encryptionKey = MatrixUtil.ENCRYPTION_KEYS.get(encryptionKeyId);

		// Allocate memory
		size_t pickledLength = LIB_OLM.olm_pickle_session_length(this);
		Memory pickledBuffer = malloc(pickledLength);
		Buffer keyBuffer = malloc(encryptionKey.getBytes(UTF_8));

		// serialize session
		size_t errorCode = LIB_OLM.olm_pickle_session(this, keyBuffer, keyBuffer.size(), pickledBuffer, pickledLength);
		if (errorCode.equals(LIB_OLM.olm_error()))
			throw new LibOlm.OlmInvokationException("olm_pickle_session", LIB_OLM.olm_session_last_error(this));

		return JNAUtil.getString(pickledBuffer);
	}

	public int getNextMessageType() {
		size_t messageType = LIB_OLM.olm_encrypt_message_type(this);
		if (messageType.equals(LIB_OLM.olm_error()))
			throw new LibOlm.OlmInvokationException("olm_encrypt_message_type", LIB_OLM.olm_session_last_error(this));

		return messageType.intValue();
	}

	public String encrypt(String plaintext) {
		byte[] plaintextData = plaintext.getBytes(UTF_8);

		// Create random data and allocate memory
		Buffer plaintextBuffer = malloc(plaintextData);
		size_t randomLength = LIB_OLM.olm_encrypt_random_length(this);
		size_t messageLength = LIB_OLM.olm_encrypt_message_length(this, plaintextBuffer.size());
		Buffer randomBuffer = JNAUtil.random(randomLength);
		Memory messageBuffer = malloc(messageLength);

		// Encrypt message
		size_t errorCode = LIB_OLM.olm_encrypt(this, plaintextBuffer, plaintextBuffer.size(), randomBuffer, randomLength, messageBuffer, messageLength);
		if (errorCode.equals(LIB_OLM.olm_error()))
			throw new LibOlm.OlmInvokationException("olm_encrypt", LIB_OLM.olm_session_last_error(this));

		String s = JNAUtil.getString(messageBuffer);
		return s;
	}

	@Override
	protected void finalize() throws Throwable {
		LIB_OLM.olm_clear_session(this);
		super.finalize();
	}

}