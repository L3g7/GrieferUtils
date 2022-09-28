/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.util;

import com.google.gson.*;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.Consumer;

public class IOUtil {

/*
	TODO:
	IOUtil concept:


	Sync file read:
		IOUtil.file(configFile)
		.readJsonObject(v -> config = v)
		.orElse(t -> config = new JsonObject());

	config = IOUtil.read(configFile)
		.asJsonObject()
        .orElse(t -> new JsonObject());

	Sync url read:
		IOUtil.request("https://api.../releases")
		.asJsonArray(releases -> {/* stuff TM * /});

	releases = IOUtil.read("https://api.../releases")
		.asJsonArray();

	Async url read:
		IOUtil.request(url)
		.asJsonArray(array -> {/* stuff TM * /});

    IOUtil.read(url)
		.asJsonArray(array -> {/* stuff TM * /});

	Async url write:
		IOUtil.request("https://grieferutils.l3g7.dev/analytics/")
		.post("application/json", data.toString().getBytes(UTF_8))
		.close();

    IOUtil.write("https://grieferutils.l3g7.dev/analytics/")
	    .json("application/json", data);
	*/

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static final JsonParser JSON_PARSER = new JsonParser();

	public static FileRequest file(File file) {
		return new FileRequest(file);
	}

	public static HttpRequest request(String url) {
		return new HttpRequest(url.trim());
	}

	public static String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static class FileRequest {

		private final File file;
		private Throwable error = null;

		private FileRequest(File file) {
			this.file = file;
		}

		public void writeJson(JsonElement element) {
			file.getParentFile().mkdirs();
			try (OutputStreamWriter ow = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
				ow.write(GSON.toJson(element));
			} catch (Throwable e) {
				e.printStackTrace();
				error = e;
			}
		}

		public FileRequest readJsonObject(Consumer<JsonObject> consumer) {
			if (!file.exists() || file.isDirectory()) {
				error = new IOException(file.exists() ? "file is directory" : "file does not exist");
			}

			if (error != null)
				return this;

			try (InputStreamReader in = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)) {
				consumer.accept(JSON_PARSER.parse(in).getAsJsonObject());
			} catch (Throwable e) {
				e.printStackTrace();
				error = e;
			}
			return this;
		}

		public void orElse(Consumer<Throwable> callback) {
			if (error != null)
				callback.accept(error);
		}

	}

	public static class HttpRequest {

		private HttpURLConnection conn;
		private volatile Throwable error = null;
		private volatile Consumer<Throwable> errorCallback = null;
		private volatile boolean shouldClose = false;

		private HttpRequest(String url) {
			try {
				conn = (HttpURLConnection) new URL(url).openConnection();
				conn.setConnectTimeout(3000);
				conn.setReadTimeout(10000);
				conn.addRequestProperty("User-Agent", "GrieferUtils");
			} catch (Throwable e) {
				e.printStackTrace();
				error = e;
			}
		}

		public HttpRequest post(String contentType, byte[] data) {
			tryAsync(() -> {
				conn.addRequestProperty("Content-Type", contentType);
				conn.setDoOutput(true);

				conn.setRequestMethod("POST");

				try (OutputStream stream = conn.getOutputStream()) {
					stream.write(data);
					stream.flush();
				}
			});
			return this;
		}

		public HttpRequest asJsonArray(Consumer<JsonArray> consumer) {
			tryAsync(() -> {
				try (InputStreamReader in = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
					consumer.accept(JSON_PARSER.parse(in).getAsJsonArray());
				}
			});
			return this;
		}

		public HttpRequest asJsonString(Consumer<String> consumer) {
			tryAsync(() -> {
				try (InputStreamReader in = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
					consumer.accept(JSON_PARSER.parse(in).getAsString());
				}
			});
			return this;
		}

		public HttpRequest asString(Consumer<String> consumer) {
			tryAsync(() -> {
				try (InputStream in = conn.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
					byte[] buffer = new byte[4096];

					int n;
					while ((n = in.read(buffer)) != -1)
						out.write(buffer, 0, n);

					consumer.accept(out.toString("UTF-8"));
				}
			});
			return this;
		}

		public int getResponseCode() {
			try {
				return conn.getResponseCode();
			} catch (Throwable e) {
				e.printStackTrace();
				return -1;
			}
		}

		public void orElse(Consumer<Throwable> callback) {
			errorCallback = callback;
			if (error != null)
				callback.accept(error);
		}

		public void close() {
			shouldClose = true;
		}

		public boolean successful() {
			return error == null;
		}

		private static final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("GrieferUtils' IO", false, Thread.MIN_PRIORITY));

		private void tryAsync(AsyncAction action) {
			if (error != null)
				return;

			Throwable stackTrace = new Throwable();
			eventLoopGroup.submit(() -> {
				try {
					action.run();
					if (shouldClose) {
						conn.getInputStream().close();
						conn.disconnect();
						shouldClose = false;
					}
				} catch (Throwable e) {
					e.printStackTrace();
					stackTrace.printStackTrace();
					error = e;
					if (errorCallback != null)
						errorCallback.accept(e);
				}
			});
		}

		private interface AsyncAction {

			void run() throws IOException;

		}
	}

}