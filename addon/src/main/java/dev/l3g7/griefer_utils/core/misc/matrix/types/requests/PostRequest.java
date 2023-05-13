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

import dev.l3g7.griefer_utils.core.util.IOUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class PostRequest<R> extends Request<R> {

	private static final AtomicLong TX_COUNTER = new AtomicLong();

	public PostRequest(String path) {
		super(path);
	}

	@Override
	protected void updateConnection(HttpURLConnection connection) throws IOException {
		connection.setDoOutput(true);
		connection.setRequestMethod(getRequestMethod());
		connection.getOutputStream().write(serialize().getBytes(UTF_8));
	}

	protected String serialize() {
		return IOUtil.gson.toJson(this);
	}

	protected String getRequestMethod() {
		return "POST";
	}

	protected static String newTxId() {
		return System.currentTimeMillis() + "." + TX_COUNTER.getAndIncrement();
	}

	public abstract static class PutRequest<R> extends PostRequest<R> {

		public PutRequest(String path) {
			super(path);
		}

		@Override
		public String getRequestMethod() {
			return "PUT";
		}

	}
}