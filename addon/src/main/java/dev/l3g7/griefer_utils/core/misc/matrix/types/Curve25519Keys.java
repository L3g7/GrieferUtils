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

import dev.l3g7.griefer_utils.core.misc.matrix.MatrixUtil;

import java.util.HashMap;
import java.util.Map;

public class Curve25519Keys {

	public Map<String, String> curve25519;

	public Map<String, SignedCurve25519Key> sign(Session session, boolean isFallback) {
		Map<String, SignedCurve25519Key> signedKeys = new HashMap<>();
		curve25519.forEach((key, value) -> {
			SignedCurve25519Key signedKey = new SignedCurve25519Key(isFallback ? true : null, value);
			signedKey.addSignature(session.userId, session.deviceId, session.olmAccount.sign(MatrixUtil.toSignaturePayload(signedKey)));
			signedKeys.put("signed_curve25519:" + key, signedKey);
		});
		return signedKeys;
	}

	public static class SignedCurve25519Key {

		public Boolean fallback;
		public String key;
		public transient Map<String, Map<String, String>> signatures;

		public SignedCurve25519Key(Boolean fallback, String key) {
			this.fallback = fallback;
			this.key = key;
			this.signatures = new HashMap<>();
		}

		public void addSignature(String userId, String deviceId, String signature) {
			signatures.computeIfAbsent(userId, n -> new HashMap<>()).put("ed25519:" + deviceId, signature);
		}

	}
}