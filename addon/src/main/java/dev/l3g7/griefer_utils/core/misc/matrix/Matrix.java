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

package dev.l3g7.griefer_utils.core.misc.matrix;

import dev.l3g7.griefer_utils.core.misc.matrix.jna.structures.OlmAccount;
import dev.l3g7.griefer_utils.core.misc.matrix.modules.uiaa.PlayerKeyPair;
import dev.l3g7.griefer_utils.core.misc.matrix.requests.AuthorizeRequest.LoginRequest;
import dev.l3g7.griefer_utils.core.misc.matrix.requests.AuthorizeRequest.RegisterRequest;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Matrix {

	public static CompletableFuture<Session> register(String host, UUID uuid, String password, String deviceDisplayName, PlayerKeyPair playerKeyPair) {
		return new RegisterRequest("minecraft-" + uuid, password, deviceDisplayName, playerKeyPair).sendAsync(new Session(host)).thenApply(authData -> {
			OlmAccount account = OlmAccount.create("");// TODO generate encryption key ID
			return new Session(host, authData, account);
		});
	}

	public static CompletableFuture<Session> login(String host, UUID uuid, PlayerKeyPair playerKeyPair) {
		return new LoginRequest("minecraft-" + uuid, playerKeyPair).sendAsync(new Session(host)).thenApply(authData -> {
			OlmAccount account = OlmAccount.create("");// TODO generate encryption key ID
			return new Session(host, authData, account);
		});
	}

}