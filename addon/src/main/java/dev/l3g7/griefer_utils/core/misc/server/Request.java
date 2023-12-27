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

package dev.l3g7.griefer_utils.core.misc.server;

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

import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class Request<R> {

	public transient final String path;

	public Request(String path) {
		this.path = path;
	}

	protected String serialize() {
		return IOUtil.gson.toJson(this);
	}

	protected abstract R parseResponse(GUSession session, Response response) throws Throwable;

	public R send(GUSession session) {
		return send(session, BugReporter::reportError);
	}

	public R send(GUSession session, Consumer<IOException> errorHandler) {
		try {
			return send(session, false);
		} catch (IOException e) {
			errorHandler.accept(e);
			return null;
		}
	}

	private R send(GUSession session, boolean sessionRenewed) throws IOException {
		HttpsURLConnection conn = (HttpsURLConnection) new URL(session.host + path).openConnection();
		conn.setSSLSocketFactory(CustomSSLSocketFactoryProvider.getCustomFactory());

		conn.setRequestProperty("Content-Type", "application/json");
		if (session.sessionToken != null)
			conn.setRequestProperty("Authorization", "Bearer " + session.sessionToken);

		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.getOutputStream().write(serialize().getBytes(UTF_8));

		// Renew token if authorization fails
		if (conn.getResponseCode() == HTTP_UNAUTHORIZED) {
			if (sessionRenewed)
				return null;

			try {
				session.renewToken();
				return send(session, true);
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