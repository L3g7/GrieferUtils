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

package dev.l3g7.griefer_utils.core.misc.matrix.events.cross_signing;

import dev.l3g7.griefer_utils.core.misc.matrix.events.Event;
import dev.l3g7.griefer_utils.core.misc.matrix.events.Event.EventContent;
import dev.l3g7.griefer_utils.core.misc.matrix.modules.ssss.AesHmacSha2EncryptedData;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;

import java.security.GeneralSecurityException;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Event(key = "m.cross_signing.self_signing")
public class CrossSigningSelfSigningEvent extends EventContent {

	public Map<String, AesHmacSha2EncryptedData> encrypted;

	@Override
	public void handle(Session session) {
		try {
			String defaultKeyId = session.keyStore.defaultKeyId;
			byte[] key = session.keyStore.ssssKey.decrypt("m.cross_signing.self_signing", encrypted.get(defaultKeyId));
			session.keyStore.selfSigningKey.set(new String(key, UTF_8));
		} catch (GeneralSecurityException e) {
			System.err.println("Could not decipher self-signing key");
			e.printStackTrace();
		}
	}

}