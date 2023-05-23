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

import dev.l3g7.griefer_utils.core.misc.matrix.modules.uiaa.MinecraftAuthentication.MinecraftAuthenticationMethod;
import dev.l3g7.griefer_utils.core.misc.matrix.modules.uiaa.MinecraftAuthentication.UiaaMinecraftAuthFlowResponse;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.Response;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.Arrays;
import java.util.Base64;

public class UiaaHandler {

	private static final String[] SUPPORTED_UIAA_FLOW = new String[]{ "dev.l3g7.minecraft_auth.auth.minecraft" };

	public static MinecraftAuthenticationMethod createAuthentication(Response rawResponse, PlayerKeyPair playerKeyPair) throws GeneralSecurityException {
		// Parse and validate response
		UiaaFlow.UiaaFlowResponse uiaaResponse = rawResponse.convertTo(UiaaFlow.UiaaFlowResponse.class);
		if (uiaaResponse.flows.stream().noneMatch(flow -> Arrays.equals(flow.stages, SUPPORTED_UIAA_FLOW)))
			throw new UnsupportedOperationException("Server does not support minecraft auth flow!");

		// Create authentication signature
		UiaaMinecraftAuthFlowResponse response = rawResponse.convertTo(UiaaMinecraftAuthFlowResponse.class);
		byte[] nonce = Base64.getDecoder().decode(response.params.nonce);

		// Sign nonce
		Signature sign = Signature.getInstance("SHA256withRSA");
		sign.initSign(playerKeyPair.getPrivateKey());
		sign.update(nonce);
		byte[] signature = sign.sign();

		return new MinecraftAuthenticationMethod(response.session, Base64.getEncoder().encodeToString(signature), playerKeyPair);
	}

}