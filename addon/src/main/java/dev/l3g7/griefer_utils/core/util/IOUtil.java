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

package dev.l3g7.griefer_utils.core.util;

import com.google.gson.*;
import dev.l3g7.griefer_utils.core.misc.functions.Consumer;
import dev.l3g7.griefer_utils.core.misc.functions.Function;
import dev.l3g7.griefer_utils.util.AddonUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import static dev.l3g7.griefer_utils.core.util.Util.elevate;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A utility class for simplified file and network operations.
 * All operations use an encoding of UTF-8.
 */
public class IOUtil {

	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public static final JsonParser jsonParser = new JsonParser();

	/**
	 * @return A wrapper class for reading the contents of the given file.
	 */
	public static FileReadOperation read(File file) {
		return new FileReadOperation(file);
	}

	/**
	 * @return A wrapper class for reading the contents of the given url.
	 */
	public static URLReadOperation read(String url) {
		return new URLReadOperation(url);
	}

	/**
	 * Writes the content to the file.
	 */
	public static void write(File file, String content) {
		file.getParentFile().mkdirs();

		try {
			Files.write(file.toPath(), content.getBytes(UTF_8));
		} catch (IOException e) {
			throw elevate(e);
		}
	}

	/**
	 * Writes the content it to the file using pretty printing.
	 */
	public static void writeJson(File file, JsonElement content) {
		write(file, gson.toJson(content));
	}

	/**
	 * Sends the given json to the URL using a POST request.
	 */
	public static void writeJson(String url, JsonElement json) {
		Throwable trigger = new Throwable("Invoker stack trace:");
		Thread t = new Thread(() -> {
			try {
				HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
				conn.addRequestProperty("User-Agent", "GrieferUtils v" + AddonUtil.getVersion());
				conn.setConnectTimeout(10000);
				conn.setDoOutput(true);
				conn.addRequestProperty("Content-Type", "application/json");
				conn.setRequestMethod("POST");

				try (OutputStream out = conn.getOutputStream()) {
					out.write(json.toString().getBytes(UTF_8));
					out.flush();
				}
				conn.getInputStream().close();
				conn.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				trigger.printStackTrace();
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	/**
	 * A wrapper class for reading the contents of a file.
	 */
	public static class FileReadOperation extends ReadOperation {

		private final File file;

		private FileReadOperation(File file) {
			this.file = file;
		}

		@Override
		protected InputStream open() throws Exception {
			return Files.newInputStream(file.toPath(), StandardOpenOption.READ);
		}

	}

	/**
	 * A wrapper class for reading the contents of an url.
	 */
	public static class URLReadOperation extends ReadOperation {

		private final String url;
		private HttpURLConnection conn;

		private URLReadOperation(String url) {
			this.url = url;
		}

		@Override
		protected InputStream open() throws Exception {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.addRequestProperty("User-Agent", "GrieferUtils v" + AddonUtil.getVersion());
			conn.setConnectTimeout(10000);
			return conn.getInputStream();
		}

		public int getResponseCode() {
			try {
				open();
				return conn.getResponseCode();
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}
		}
	}

	public abstract static class ReadOperation {

		protected abstract InputStream open() throws Exception;

		/**
		 * Tries to read the input stream as a json object.
		 */
		public Optional<JsonObject> asJsonObject() {
			return readSync(in -> jsonParser.parse(in).getAsJsonObject());
		}

		/**
		 * Tries to read the input stream as a json string.
		 */
		public AsyncFailable asJsonString(Consumer<String> callback) {
			return readAsync(in -> jsonParser.parse(in).getAsString(), callback);
		}

		/**
		 * Tries to read the input stream as a json array.
		 */
		public AsyncFailable asJsonArray(Consumer<JsonArray> callback) {
			return readAsync(in -> jsonParser.parse(in).getAsJsonArray(), callback);
		}

		/**
		 * Tries to read the input stream as a json object.
		 */
		public AsyncFailable asJsonObject(Consumer<JsonObject> callback) {
			return readAsync(in -> jsonParser.parse(in).getAsJsonObject(), callback);
		}

		/**
		 * Tries to open an input stream.
		 */
		public Optional<InputStream> getStream() {
			try {
				return Optional.of(open());
			} catch (Exception e) {
				e.printStackTrace();
				return Optional.empty();
			}
		}

		/**
		 * Tries to write the input stream to the given file.
		 */
		public AsyncFailable asFile(File file, Consumer<File> callback) {
			AsyncFailable op = new AsyncFailable();
			Throwable trigger = new Throwable("Invoker stack trace:");
			Thread t = new Thread(() -> {
				try {
					try (InputStream in = open(); FileOutputStream out = new FileOutputStream(file)) {
						byte[] buffer = new byte[4096];

						int n;
						while ((n = in.read(buffer)) != -1)
							out.write(buffer, 0, n);

						callback.accept(file);
					}
				} catch (Exception e) {
					trigger.printStackTrace();
					e.printStackTrace();
					if (op.fallback != null)
						op.fallback.accept(e);
				}
			});
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
			return op;
		}

		/**
		 * Tries to read the input stream using the given parser.
		 * <br>
		 * Example:
		 * <pre>{@code
		 * obj = IOUtil.read(file)
		 *     .asJsonObject()
		 *     .orElse(new JsonObject());
		 * }</pre>
		 * @return the value given by the supplier or an empty optional if the supplier throws an error.
		 */
		private <V> Optional<V> readSync(Function<InputStreamReader, V> parser) {
			try {
				try (InputStreamReader in = new InputStreamReader(open(), UTF_8)) {
					return Optional.of(parser.apply(in));
				}
			} catch (Exception e) {
				e.printStackTrace();
				return Optional.empty();
			}
		}

		/**
		 * Tries to read the input stream using the given parser.
		 * <br>
		 * Example:
		 * <pre>{@code
		 * IOUtil.read(url)
		 *     .asJsonString(str -> log("success", str))
		 *     .orElse(error -> log("error", error));
		 * }</pre>
		 */
		private <V> AsyncFailable readAsync(Function<InputStreamReader, V> parser, Consumer<V> callback) {
			AsyncFailable op = new AsyncFailable();
			Throwable trigger = new Throwable("Invoker stack trace:");
			Thread t = new Thread(() -> {
				try {
					try (InputStreamReader in = new InputStreamReader(open(), UTF_8)) {
						callback.accept(parser.apply(in));
					}
				} catch (Exception e) {
					trigger.printStackTrace();
					e.printStackTrace();
					if (op.fallback != null)
						op.fallback.accept(e);
				}
			});
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
			return op;
		}

	}

	public static class AsyncFailable {

		private java.util.function.Consumer<Exception> fallback;

		public void orElse(Runnable fallback) {
			this.fallback = t -> fallback.run();
		}

	}
}