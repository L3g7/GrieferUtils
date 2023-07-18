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

import at.favre.lib.crypto.HKDF;
import dev.l3g7.griefer_utils.core.misc.functions.Runnable;
import dev.l3g7.griefer_utils.core.misc.matrix.jna.structures.OlmAccount;
import dev.l3g7.griefer_utils.core.misc.matrix.modules.SyncHandler;
import dev.l3g7.griefer_utils.core.misc.matrix.modules.uiaa.PlayerKeyPair;
import dev.l3g7.griefer_utils.core.misc.matrix.requests.*;
import dev.l3g7.griefer_utils.core.misc.matrix.requests.AuthorizeRequest.LoginRequest;
import dev.l3g7.griefer_utils.core.misc.matrix.requests.WhoamiRequest.WhoamiResponse;
import dev.l3g7.griefer_utils.core.misc.matrix.types.AuthData;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;
import dev.l3g7.griefer_utils.core.misc.matrix.types.User;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class Matrix {

	/**
	 * A salt used for deriving passwords from username and private key.
	 */
	private static final byte[] SALT = Base64.getDecoder().decode("Ju7XvMcjOvrABD9WmOnnKw==");

	private static final String HOST = "https://l3g7.dev";

	private final String host;
	private SyncHandler syncHandler;
	public Session session;

	public Matrix() throws IOException {

		// Resolve well-known URI
		this.host = new WellKnownRequest().send(new Session(HOST));
		this.session = new Session(this.host);

		Runtime.getRuntime().addShutdownHook(new Thread((Runnable) () -> {
			if (session.authData != null)
				new LogoutRequest().send(session);
		}));
	}

	protected void sync() throws IOException {
		if (syncHandler != null)
			syncHandler.stop();

		syncHandler = new SyncHandler(session);
		syncHandler.start();
	}

	/**
	 * Tries to register or, if the username is not available, login.
	 */
	protected void authorize(String username, String authToken) throws IOException {
		CompletableFuture<PlayerKeyPair> keyPairRequest = PlayerKeyPair.getPlayerKeyPair(authToken);
		Boolean available = new RegisterAvailableRequest(username).send(session);
		if (available == null)
			// Connection cannot be initiated as username is invalid
			return;

		PlayerKeyPair keyPair = keyPairRequest.join();

		if (keyPair == null)
			return;

		if (available) {
			// Derive password from private key and username
			String password = new String(HKDF.fromHmacSha512().extractAndExpand(SALT, keyPair.getRawPrivateKey(), username.getBytes(UTF_8), 32), UTF_8);

			// Register
			AuthData authData = new AuthorizeRequest.RegisterRequest(username, password, keyPair).send(session);
			session = new Session(host, authData, OlmAccount.create());
		} else {
			// Login
			AuthData authData = new LoginRequest(username, keyPair).send(session);
			session = new Session(host, authData, OlmAccount.create());
		}
	}

	protected void updateUsingWhoami() throws IOException {
		WhoamiResponse whoamiResponse = new WhoamiRequest().send(session);
		session.userId = whoamiResponse.user_id;
		session.deviceId = whoamiResponse.device_id;
	}

	protected void setDisplayName(String displayName) throws IOException {
		new ProfileDisplaynamePutRequest(session.userId, displayName).send(session);
	}

	protected List<User> fullSearch(String searchTerm) throws IOException {
		return new FullSearchRequest(searchTerm).send(session);
	}

	public List<UUID> getOnlineUsers(Set<UUID> requestedUsers) throws IOException {
		return new OnlineUsersRequest(requestedUsers).send(session);
	}

}