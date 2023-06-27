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
import dev.l3g7.griefer_utils.core.misc.matrix.jna.LibOlm;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.util.Buffer;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.util.JNAUtil;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.util.size_t;

import static dev.l3g7.griefer_utils.core.misc.matrix.jna.LibOlm.LIB_OLM;
import static dev.l3g7.griefer_utils.core.misc.matrix.jna.util.JNAUtil.malloc;
import static java.nio.charset.StandardCharsets.UTF_8;

public class OlmOutboundGroupSession extends PointerType {

	/**
	 * Has no references once set, only used to prevent the Memory object from being garbage
	 * -collected and freeing the underlying memory.
	 */
	@SuppressWarnings("unused")
	private Memory allocatedMemory;

	private static OlmOutboundGroupSession allocateSession() {
		// Allocate memory
		size_t sessionSize = LIB_OLM.olm_outbound_group_session_size();
		Memory sessionBuffer = malloc(sessionSize);

		// Initialize session
		OlmOutboundGroupSession session = LIB_OLM.olm_outbound_group_session(sessionBuffer);
		session.allocatedMemory = sessionBuffer;
		return session;
	}

	public static OlmOutboundGroupSession create() { // TODO Rotate megolm session
		OlmOutboundGroupSession session = allocateSession();

		// Create random data
		size_t randomLength = LIB_OLM.olm_init_outbound_group_session_random_length(session);
		Buffer randomBuffer = JNAUtil.random(randomLength);

		// Create session
		size_t errorCode = LIB_OLM.olm_init_outbound_group_session(session, randomBuffer, randomLength);
		if (errorCode.equals(LIB_OLM.olm_error()))
			throw new LibOlm.OlmInvokationException("olm_init_outbound_group_session", LIB_OLM.olm_outbound_group_session_last_error(session));

		return session;
	}

	public String getSessionId() {
		// Allocate memory
		size_t identifierLength = LIB_OLM.olm_outbound_group_session_id_length(this);
		Memory identifierBuffer = malloc(identifierLength);

		// Get identifier
		size_t errorCode = LIB_OLM.olm_outbound_group_session_id(this, identifierBuffer, identifierLength);
		if (errorCode.equals(LIB_OLM.olm_error()))
			throw new LibOlm.OlmInvokationException("olm_outbound_group_session_id", LIB_OLM.olm_outbound_group_session_last_error(this));

		return JNAUtil.getString(identifierBuffer);
	}

	public String getSessionKey() {
		// Allocate memory
		size_t keyLength = LIB_OLM.olm_outbound_group_session_key_length(this);
		Memory keyBuffer = malloc(keyLength);

		// Get key
		size_t errorCode = LIB_OLM.olm_outbound_group_session_key(this, keyBuffer, keyLength);
		if (errorCode.equals(LIB_OLM.olm_error()))
			throw new LibOlm.OlmInvokationException("olm_outbound_group_session_key", LIB_OLM.olm_outbound_group_session_last_error(this));

		return JNAUtil.getString(keyBuffer);
	}

	public String encrypt(String plaintext) {
		byte[] plaintextData = plaintext.getBytes(UTF_8);

		// Allocate memory
		Buffer plaintextBuffer = malloc(plaintextData);
		size_t messageLength = LIB_OLM.olm_group_encrypt_message_length(this, plaintextBuffer.size());
		Memory messageBuffer = malloc(messageLength);

		// Encrypt message
		size_t errorCode = LIB_OLM.olm_group_encrypt(this, plaintextBuffer, plaintextBuffer.size(), messageBuffer, messageLength);
		if (errorCode.equals(LIB_OLM.olm_error()))
			throw new LibOlm.OlmInvokationException("olm_group_encrypt", LIB_OLM.olm_outbound_group_session_last_error(this));

		String s = JNAUtil.getString(messageBuffer);
		return s;
	}

}