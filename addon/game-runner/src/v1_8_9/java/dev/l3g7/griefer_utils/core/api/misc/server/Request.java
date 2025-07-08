/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc.server;

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.api.BugReporter;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.misc.CustomSSLSocketFactoryProvider;
import dev.l3g7.griefer_utils.core.api.misc.PlayerKeyPair;
import dev.l3g7.griefer_utils.core.api.misc.ThreadFactory;
import dev.l3g7.griefer_utils.core.api.misc.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.util.IOUtil;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.MIN_PRIORITY;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class Request<R> {

	protected static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newScheduledThreadPool(
		4, new ThreadFactory("grieferutils-server-conn-%d", MIN_PRIORITY));

	public transient final String server, path;

	public Request(String server, String path) {
		this.server = server;
		this.path = path;
	}

	protected String serialize() {
		return IOUtil.gson.toJson(this);
	}

	protected abstract R parseResponse(Response response) throws Throwable;

	public R send() {
		return request(BugReporter::reportError, true);
	}

	public R get() {
		return request(BugReporter::reportError, false);
	}

	// NOTE: handle errors at a higher level, the current solution is very confusing and prone to errors if anything changes
	public R request(Consumer<IOException> errorHandler, boolean post) {
		// Try 3 times
		IOException[] exceptions = new IOException[3];
		for (int attempt = 0; attempt < 3; attempt++) {
			try {
				return request(false, post);
			} catch (IOException e) {
				exceptions[attempt] = e;
			}
		}

		// All tries failed, try again in 10 min
		Arrays.stream(exceptions).forEach(Throwable::printStackTrace);
		try {
			return SCHEDULED_EXECUTOR.schedule(() -> request(errorHandler, post), 10, TimeUnit.MINUTES).get();
		} catch (ExecutionException | InterruptedException e) {
			errorHandler.accept(new IOException(e));
			return null;
		}
	}

	protected R request(boolean sessionRenewed, boolean post) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(server + path).openConnection();
		if (conn instanceof HttpsURLConnection httpsConn)
			httpsConn.setSSLSocketFactory(CustomSSLSocketFactoryProvider.getCustomFactory());

		conn.addRequestProperty("User-Agent", "GrieferUtils v" + LabyBridge.labyBridge.addonVersion() + " | github.com/L3g7/GrieferUtils");
		if (GUServer.isAvailable())
			conn.setRequestProperty("Authorization", GUServer.generateAuthHeader());

		if (post) {
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.getOutputStream().write(serialize().getBytes(UTF_8));
		}

		// Renew token if authorization fails
		if (conn.getResponseCode() == HTTP_UNAUTHORIZED) {
			if (sessionRenewed)
				return null;

			GUServer.renewToken();
			return request(true, post);
		}

		InputStream in = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
		Response r = new Response(conn.getResponseCode(), new String(IOUtil.toByteArray(in), StandardCharsets.UTF_8));

		try {
			return parseResponse(r);
		} catch (Throwable e) {
			throw new IOException(e);
		}
	}

	public static class Response {

		private final int status;
		private final String body;

		public Response(int status, String body) {
			this.status = status;
			this.body = body;
		}

		public int getStatus() {
			return status;
		}

		public <T> T convertTo(Class<T> type) {
			return IOUtil.gson.fromJson(body, type);
		}
	}

	@SuppressWarnings({"FieldCanBeLocal", "unused"}) // Class is serialized with gson
	static class AuthData {

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

		AuthData(UUID user, PlayerKeyPair keyPair) throws GeneralSecurityException {
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
	}

}
