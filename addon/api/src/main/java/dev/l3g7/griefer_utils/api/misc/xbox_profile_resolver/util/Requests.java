/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.api.misc.xbox_profile_resolver.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.l3g7.griefer_utils.api.misc.CustomSSLSocketFactoryProvider;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Requests {

	public static JsonElement post(String url, Map<String, String> headers, Map<String, String> data) throws IOException {
		return post(url, headers, Util.urlEncode(data).getBytes(UTF_8), "application/x-www-form-urlencoded");
	}

	public static JsonElement post(String url, Map<String, String> headers, JsonElement data) throws IOException {
		return post(url, headers, data.toString().getBytes(UTF_8), "application/json");
	}

	public static JsonElement post(String url, Map<String, String> headers, byte[] payload, String contentType) throws IOException {
		headers.putAll(Util.strMap(
				"Accept", "*/*",
				"Accept-Encoding", "identity",
				"Content-Length", Integer.toString(payload.length),
				"Content-Type", contentType,
				"User-Agent", "GrieferUtils"
		));

		return request(url, "POST", headers, payload);
	}

	public static JsonElement get(String url, Map<String, String> headers) throws IOException {
		return request(url, "GET", headers, null);
	}

	private static JsonElement request(String url, String method, Map<String, String> headers, byte[] payload) throws IOException {
		HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
		conn.setSSLSocketFactory(CustomSSLSocketFactoryProvider.getCustomFactory());
		conn.setRequestMethod(method);
		headers.forEach(conn::setRequestProperty);
		if (payload != null) {
			conn.setDoOutput(true);
			try (OutputStream out = conn.getOutputStream()) {
				out.write(payload);
				out.flush();
			}
		}

		return new JsonParser().parse(new InputStreamReader(conn.getInputStream()));
	}

}
