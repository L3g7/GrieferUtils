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
import dev.l3g7.griefer_utils.core.misc.matrix.jna.util.size_t;

import static dev.l3g7.griefer_utils.core.misc.matrix.jna.LibOlm.LIB_OLM;
import static dev.l3g7.griefer_utils.core.misc.matrix.jna.util.JNAUtil.malloc;
import static java.nio.charset.StandardCharsets.UTF_8;

public class OlmInboundGroupSession extends PointerType {

	public static OlmInboundGroupSession create(String sessionKey) {
		// Allocate memory
		size_t sessionSize = LIB_OLM.olm_inbound_group_session_size();
		Memory sessionBuffer = malloc(sessionSize);
		Buffer keyBuffer = malloc(sessionKey.getBytes(UTF_8));

		// Initialize session
		OlmInboundGroupSession session = LIB_OLM.olm_inbound_group_session(sessionBuffer);

		// Create session
		size_t errorCode = LIB_OLM.olm_init_inbound_group_session(session, keyBuffer, keyBuffer.size());
		if (errorCode.equals(LIB_OLM.olm_error()))
			throw new LibOlm.OlmInvokationException("olm_init_inbound_group_session", LIB_OLM.olm_inbound_group_session_last_error(session));

		return session;
	}

	@Override
	protected void finalize() throws Throwable {
		LIB_OLM.olm_clear_inbound_group_session(this);
		super.finalize();
	}

}