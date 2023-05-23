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

package dev.l3g7.griefer_utils.core.misc.matrix.types.cryptography;

import dev.l3g7.griefer_utils.core.misc.Watchable;
import dev.l3g7.griefer_utils.core.misc.matrix.MatrixUtil;
import dev.l3g7.griefer_utils.core.misc.matrix.modules.ssss.SSSSKey;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;

import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.HashMap;

public class KeyStore {

	public final SSSSKey ssssKey = new SSSSKey();
	public String defaultKeyId;
	public final Watchable<String> selfSigningKey = new Watchable<>();

	public DeviceKeys generateDeviceKeys(Session session, String selfSigningKeyId) throws GeneralSecurityException {
		String deviceId = session.deviceId;

		DeviceKeys res = new DeviceKeys();
		res.algorithms = new String[] {"m.olm.v1.curve25519-aes-sha2", "m.megolm.v1.aes-sha2"};
		res.deviceId = deviceId;
		res.userId = session.userId;

		// Add keys
		IdentityKeys identityKeys = session.olmAccount.getIdentityKeys();
		res.keys = new HashMap<>();
		res.keys.put("curve25519:" + deviceId, identityKeys.curve25519);
		res.keys.put("ed25519:" + deviceId, identityKeys.ed25519);

		// Add account signature
		res.signatures = new HashMap<>();
		byte[] signaturePayload = MatrixUtil.toSignaturePayload(res);
		res.signatures.computeIfAbsent(session.userId, i -> new HashMap<>()).put("ed25519:" + deviceId, session.olmAccount.sign(signaturePayload));

		// Add self-signing key signature
		EdDSAEngine engine = new EdDSAEngine();
		engine.initSign(new EdDSAPrivateKey(new EdDSAPrivateKeySpec(Base64.getDecoder().decode(session.keyStore.selfSigningKey.get()), EdDSANamedCurveTable.ED_25519_CURVE_SPEC)));
		String enc = Base64.getEncoder().encodeToString(engine.signOneShot(signaturePayload));
		res.signatures.computeIfAbsent(session.userId, i -> new HashMap<>()).put("ed25519:" + selfSigningKeyId, enc);
		return res;
	}
}