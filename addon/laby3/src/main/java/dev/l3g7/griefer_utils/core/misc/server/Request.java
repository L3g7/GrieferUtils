/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.misc.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.l3g7.griefer_utils.core.misc.CustomSSLSocketFactoryProvider;
import dev.l3g7.griefer_utils.core.misc.functions.Consumer;
import dev.l3g7.griefer_utils.core.misc.server.types.GUSession;
import dev.l3g7.griefer_utils.core.util.IOUtil;
import dev.l3g7.griefer_utils.features.uncategorized.BugReporter;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class Request<R> {

	protected static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newScheduledThreadPool(
		4, new ThreadFactoryBuilder()
			.setPriority(Thread.MIN_PRIORITY)
			.setNameFormat("grieferutils-server-conn-%d")
			.build());

	public transient final String path;

	public Request(String path) {
		this.path = path;
	}

	protected String serialize() {
		return IOUtil.gson.toJson(this);
	}

	protected abstract R parseResponse(GUSession session, Response response) throws Throwable;

	public R send(GUSession session) {
		return request(session, BugReporter::reportError, true);
	}

	public R get(GUSession session) {
		return request(session, BugReporter::reportError, false);
	}

	// TODO: handle errors at a higher level, the current solution is very confusing and prone to errors if anything changes
	public R request(GUSession session, Consumer<IOException> errorHandler, boolean post) {
		// Try 3 times
		IOException[] exceptions = new IOException[3];
		for (int attempt = 0; attempt < 3; attempt++) {
			try {
				return request(session, false, post);
			} catch (IOException e) {
				exceptions[attempt] = e;
			}
		}

		// All tries failed, try again in 10 min
		Arrays.stream(exceptions).forEach(Throwable::printStackTrace);
		try {
			return SCHEDULED_EXECUTOR.schedule(() -> request(session, errorHandler, post), 10, TimeUnit.MINUTES).get();
		} catch (ExecutionException | InterruptedException e) {
			errorHandler.accept(new IOException(e));
			return null;
		}
	}

	protected R request(GUSession session, boolean sessionRenewed, boolean post) throws IOException {
		HttpsURLConnection conn = (HttpsURLConnection) new URL(session.host + path).openConnection();
		conn.setSSLSocketFactory(CustomSSLSocketFactoryProvider.getCustomFactory());

		conn.setRequestProperty("Content-Type", "application/json");
		if (session.sessionToken != null)
			conn.setRequestProperty("Authorization", "Bearer " + session.sessionToken);

		if (post) {
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.getOutputStream().write(serialize().getBytes(UTF_8));
		}

		// Renew token if authorization fails
		if (conn.getResponseCode() == HTTP_UNAUTHORIZED) {
			if (sessionRenewed)
				return null;

			try {
				session.renewToken();
				return request(session, true, post);
			} catch (GeneralSecurityException e) {
				throw new IOException(e);
			}
		}

		InputStream in = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
		Response r = new Response(new String(IOUtils.toByteArray(in), StandardCharsets.UTF_8));

		try {
			return parseResponse(session, r);
		} catch (Throwable e) {
			throw new IOException(e);
		}
	}

}