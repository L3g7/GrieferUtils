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

package dev.l3g7.griefer_utils.core.misc.matrix.modules.uiaa;

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.misc.matrix.modules.uiaa.UiaaFlow.UiaaFlowResponse;

/**
 * Authentication by signing a nonce using a player's certificates.
 * Note that the current server implementation infers the UUID using the username.
 */
public class MinecraftAuthentication {

	/**
	 * The server response including the required parameters for authentication.
	 */
	public static class UiaaMinecraftAuthFlowResponse extends UiaaFlowResponse {

		public MinecraftAuthFlowParams params;

		public static class MinecraftAuthFlowParams {

			public String nonce;

		}
	}

	public static class MinecraftAuthenticationMethod extends AuthenticationMethod {

		public final String signature;

		@SerializedName("public_key")
		public final String publicKey;

		@SerializedName("key_signature")
		public final String keySignature;

		@SerializedName("expiration_time")
		public final long expirationTime;

		public MinecraftAuthenticationMethod(String session, String signature, PlayerKeyPair playerKeyPair) {
			super("dev.l3g7.minecraft_auth.auth.minecraft", session);
			this.signature = signature;
			this.publicKey = playerKeyPair.publicKey;
			this.keySignature = playerKeyPair.keySignature;
			this.expirationTime = playerKeyPair.expirationTime;
		}

	}
}