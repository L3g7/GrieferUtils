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

import dev.l3g7.griefer_utils.core.misc.matrix.types.Session;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public abstract class Request<R> {

	public transient final String path;

	public Request(String path) {
		this.path = path;
	}

	protected abstract R parseResponse(Session session, Response response) throws Throwable;

	public R send(Session session) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(session.host + path).openConnection();
		if (session.authData != null)
			conn.setRequestProperty("Authorization", "Bearer " + session.authData.accessToken);

		updateConnection(conn);

		InputStream in = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
		Response r = new Response(conn.getResponseCode(), new String(IOUtils.toByteArray(in), StandardCharsets.UTF_8));
		try {
			return parseResponse(session, r);
		} catch (Throwable e) {
			throw new IOException(e);
		}
	}

	public CompletableFuture<R> sendAsync(Session session) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return send(session);
			} catch (IOException e) {
				throw new CompletionException(e);
			}
		});
	}

	protected void updateConnection(HttpURLConnection builder) throws IOException {}

}