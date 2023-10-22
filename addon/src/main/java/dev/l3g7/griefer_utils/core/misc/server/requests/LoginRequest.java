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

package dev.l3g7.griefer_utils.core.misc.server.requests;

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.misc.server.types.PlayerKeyPair;
import dev.l3g7.griefer_utils.core.misc.server.Request;
import dev.l3g7.griefer_utils.core.misc.server.Response;
import dev.l3g7.griefer_utils.core.misc.server.types.GUSession;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

public class LoginRequest extends Request<String> {

	private final UUID user;

	@SerializedName("request_time")
	private final long requestTime;

	private final String signature;

	@SerializedName("public_key")
	private final String publicKey;

	@SerializedName("key_signature")
	private final String keySignature;

	@SerializedName("expiration_time")
	private final long expirationTime;

	public LoginRequest(UUID user, PlayerKeyPair keyPair) throws GeneralSecurityException {
		super("/login");
		this.user = user;

		this.requestTime = new Date().getTime();

		// Create payload
		ByteBuffer signedPayload = ByteBuffer.allocate(24);
		signedPayload.putLong(user.getMostSignificantBits());
		signedPayload.putLong(user.getLeastSignificantBits());
		signedPayload.putLong(requestTime);

		// Create signature
		Signature sign = Signature.getInstance("SHA256withRSA");
		sign.initSign(keyPair.getPrivateKey());
		sign.update(signedPayload.array());
		byte[] signature = sign.sign();

		this.signature = Base64.getEncoder().encodeToString(signature);
		this.publicKey = keyPair.getPublicKey();
		this.keySignature = keyPair.getPublicKeySignature();
		this.expirationTime = keyPair.getExpirationTime();
	}

	@Override
	protected String parseResponse(GUSession session, Response response) {
		return response.convertTo(LoginResponse.class).sessionToken;
	}

	private static class LoginResponse {

		@SerializedName("session_token")
		String sessionToken;

	}

}