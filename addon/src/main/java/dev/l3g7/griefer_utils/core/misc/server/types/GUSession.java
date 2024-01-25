/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.server.types;

import dev.l3g7.griefer_utils.core.misc.server.requests.KeepAliveRequest;
import dev.l3g7.griefer_utils.core.misc.server.requests.LoginRequest;
import dev.l3g7.griefer_utils.core.misc.server.requests.LogoutRequest;

import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.concurrent.*;

public class GUSession {

	private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

	public final String host;
	public String sessionToken;
	private ScheduledFuture<?> keepAlive;

	private UUID user;
	private String mcAuthToken;

	public GUSession(String host) {
		this.host = host;
	}

	public void login(UUID user, String mcAuthToken) throws GeneralSecurityException {
		this.user = user;
		this.mcAuthToken = mcAuthToken;

		// Get certificates
		CompletableFuture<PlayerKeyPair> keyPairRequest = PlayerKeyPair.getPlayerKeyPair(mcAuthToken);
		PlayerKeyPair keyPair = keyPairRequest.join();
		if (keyPair == null)
			return;

		// Login
		this.sessionToken = new LoginRequest(user, keyPair).send(this);
		if (sessionToken == null)
			return;

		keepAlive = SCHEDULER.scheduleAtFixedRate(() -> {
			new KeepAliveRequest().send(this);
		}, 0, 10, TimeUnit.SECONDS);
	}

	public void renewToken() throws GeneralSecurityException {
		login(user, mcAuthToken);
	}

	public void logout() {
		new LogoutRequest().send(this);

		if (keepAlive != null)
			keepAlive.cancel(false);

		keepAlive = null;
		sessionToken = null;
	}

	public boolean isValid() {
		return sessionToken != null;
	}

}
