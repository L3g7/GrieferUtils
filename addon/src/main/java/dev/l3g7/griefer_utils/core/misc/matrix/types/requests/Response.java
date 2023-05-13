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

import com.google.gson.annotations.SerializedName;
import dev.l3g7.griefer_utils.core.util.IOUtil;

public class Response {

	private final int statusCode;
	private final String body;

	public Response(int statusCode, String body) {
		this.statusCode = statusCode;
		this.body = body;
	}

	public int statusCode() {
		return statusCode;
	}

	public String body() {
		return body;
	}

	public <T> T convertTo(Class<T> type) {
		return IOUtil.gson.fromJson(body, type);
	}

	/**
	 * A generic error response.
	 */
	public static class ErrorResponse {

		@SerializedName("errcode")
		public String errorCode;
		public String error;

		public static class Exception extends IllegalStateException {

			public Exception(ErrorResponse errorResponse) {
				super("Got error response: " + errorResponse.error + " (" + errorResponse.errorCode + ")" + (errorResponse.getClass() == ErrorResponse.class ? "" : " (Handled by " + errorResponse.getClass().getName() + ")"));
			}

		}

		/**
		 * A generic <code>429: Too Many Requests</code> response.
		 */
		public static class TooManyRequestsResponse extends ErrorResponse {

			@SerializedName("retry_after_ms")
			public int retryAfterMs;

		}

	}
}