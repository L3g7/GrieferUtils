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

package dev.l3g7.griefer_utils.core.misc.matrix.types.requests;

import dev.l3g7.griefer_utils.core.misc.CustomSSLSocketFactoryProvider;
import dev.l3g7.griefer_utils.core.misc.functions.Supplier;
import dev.l3g7.griefer_utils.core.misc.matrix.MatrixUtil;
import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;
import dev.l3g7.griefer_utils.core.misc.matrix.types.requests.Response.ErrorResponse.TooManyRequestsResponse;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public abstract class Request<R> {

	/**
	 * HTTP Status-Code 429: Too Many Requests.
	 * Not specified in {@link HttpURLConnection}, so it's specified here.
	 */
	private static final int HTTP_TOO_MANY_REQUESTS = 429;

	public transient final String path;

	public Request(String path) {
		this.path = path;
	}

	protected abstract R parseResponse(Session session, Response response) throws Throwable;

	public R send(Session session) throws IOException {
		HttpsURLConnection conn = (HttpsURLConnection) new URL(session.host + path).openConnection();
		conn.setSSLSocketFactory(CustomSSLSocketFactoryProvider.getCustomFactory());

		if (session.authData != null)
			conn.setRequestProperty("Authorization", "Bearer " + session.authData.accessToken);

		updateConnection(conn);

		InputStream in = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
		Response r = new Response(conn.getResponseCode(), new String(IOUtils.toByteArray(in), StandardCharsets.UTF_8));

		if (r.statusCode() == HTTP_TOO_MANY_REQUESTS) {
			TooManyRequestsResponse response = r.convertTo(TooManyRequestsResponse.class);

			// Retry after delay
			try {
				return MatrixUtil.EXECUTOR.schedule(() -> send(session),
					response.retryAfterMs, TimeUnit.MILLISECONDS).get();
			} catch (InterruptedException | ExecutionException e) {
				throw new IOException(e);
			}
		}

		try {
			return parseResponse(session, r);
		} catch (Throwable e) {
			throw new IOException(e);
		}
	}

	public CompletableFuture<R> sendAsync(Session session) {
		return CompletableFuture.supplyAsync((Supplier<R>) () -> send(session));
	}

	protected void updateConnection(HttpURLConnection connection) throws IOException {}

}