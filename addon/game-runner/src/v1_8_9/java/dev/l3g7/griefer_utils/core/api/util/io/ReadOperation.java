package dev.l3g7.griefer_utils.core.api.util.io;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import dev.l3g7.griefer_utils.core.api.misc.ThreadFactory;
import dev.l3g7.griefer_utils.core.api.misc.primitives.containers.Result;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Consumer;
import dev.l3g7.griefer_utils.core.api.misc.primitives.functions.Supplier;
import dev.l3g7.griefer_utils.core.api.util.Util;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.MIN_PRIORITY;

public abstract class ReadOperation {

	protected static final long FALLBACK_SIZE = 32;

	protected abstract String getType();

	protected abstract InputStream getIn() throws Exception;

	protected abstract long predictSize();

	/**
	 * Reads the content into a byte array, elevating errors to RuntimeExceptions.
	 */
	public byte @NotNull [] asBytes() {
		return Util.tryFatal(() -> {
			long size = predictSize();
			if ((int) size != size)
				throw new UnsupportedOperationException("Cannot read files over 4 GB into a byte array.");

			ByteArrayOutputStream out = new ByteArrayOutputStream((int) size);

			byte[] buffer = new byte[4096];
			int read;
			try (InputStream in = getIn()) {
				while ((read = in.read(buffer, 0, 4096)) >= 0)
					out.write(buffer, 0, read);
			}

			return out.toByteArray();
		});
	}

	/**
	 * Reads the content as a UTF-8 string, elevating errors to RuntimeExceptions.
	 */
	public @NotNull String asString() {
		return new String(asBytes(), StandardCharsets.UTF_8);
	}

	/**
	 * Tries to read the content as a JSON object.
	 */
	public Result<@NotNull JsonObject> tryAsJsonObject() {
		return Result.tryGet(this::parseJsonSync)
			.map(JsonElement::getAsJsonObject);
	}

	/**
	 * Reads the content as a JSON object, elevating errors to RuntimeExceptions.
	 */
	public @NotNull JsonObject asJsonObject() {
		return tryAsJsonObject().unwrap();
	}

	/**
	 * Asynchronously reads the content as a JSON object.
	 */
	@CheckReturnValue
	public AsyncReadTask asJsonObject(Consumer<@NotNull JsonObject> callback) {
		return new AsyncReadTask(this::tryAsJsonObject, callback);
	}

	/**
	 * Tries to read the content as a JSON element.
	 */
	public Result<@NotNull JsonElement> tryAsJsonElement() {
		return Result.tryGet(this::parseJsonSync);
	}

	/**
	 * Reads the content as a JSON object, elevating errors to RuntimeExceptions.
	 */
	public @NotNull JsonElement asJsonElement() {
		return tryAsJsonElement().unwrap();
	}

	/**
	 * Tries to read the content as a JSON array.
	 */
	public Result<@NotNull JsonArray> tryAsJsonArray() {
		return Result.tryGet(this::parseJsonSync)
			.map(JsonElement::getAsJsonArray);
	}

	/**
	 * Reads the content as a JSON array, elevating errors to RuntimeExceptions.
	 */
	public @NotNull JsonArray asJsonArray() {
		return tryAsJsonArray().unwrap();
	}

	/**
	 * Asynchronously reads the content as a JSON array.
	 */
	@CheckReturnValue
	public AsyncReadTask asJsonArray(Consumer<@NotNull JsonArray> callback) {
		return new AsyncReadTask(this::tryAsJsonArray, callback);
	}

	/**
	 * Tries to read the content as a JSON string.
	 */
	public Result<@NotNull String> tryAsJsonString() {
		return Result.tryGet(this::parseJsonSync)
			.map(JsonElement::getAsString);
	}

	/**
	 * Tries to read the content as JSON.
	 */
	public <T> Result<@NotNull T> tryAsJson(TypeToken<T> type) {
		return Result.tryGet(this::parseJsonSync)
			.map(v -> IO.GSON.fromJson(v, type.getType()));
	}

	/**
	 * Reads the content as JSON, elevating errors to RuntimeExceptions.
	 */
	public <T> @NotNull T asJson(TypeToken<T> type) {
		return tryAsJson(type).unwrap();
	}

	/**
	 * GSON 2.2.4-compatible JSON parse.
	 */
	private JsonElement parseJsonSync() throws Exception {
		try (JsonReader reader = new JsonReader(new InputStreamReader(getIn(), StandardCharsets.UTF_8))) {
			JsonElement element = Streams.parse(reader);
			if (reader.peek() != JsonToken.END_DOCUMENT)
				throw new JsonSyntaxException("Trailing data");

			return element;
		}
	}

	/**
	 * A wrapper class for reading the contents of an input stream.
	 */
	protected static class InputStreamReadOperation extends ReadOperation {

		private final InputStream in;

		InputStreamReadOperation(InputStream in) {
			this.in = in;
		}

		@Override
		protected String getType() {
			return "InputStream Read";
		}

		@Override
		protected long predictSize() {
			return FALLBACK_SIZE;
		}

		@Override
		protected InputStream getIn() {
			return in;
		}

	}

	/**
	 * A wrapper class for reading the contents of an input stream without closing it.
	 */
	protected static class PartialInputStreamReadOperation extends ReadOperation {

		private final InputStream in;

		PartialInputStreamReadOperation(InputStream in) {
			this.in = new InputStream() {
				@Override
				public int read() throws IOException {
					return in.read();
				}

				@Override
				public int read(byte @NotNull [] b) throws IOException {
					return in.read(b);
				}

				@Override
				public int read(byte @NotNull [] b, int off, int len) throws IOException {
					return in.read(b, off, len);
				}

				@Override
				public long skip(long n) throws IOException {
					return in.skip(n);
				}

				@Override
				public int available() throws IOException {
					return in.available();
				}

				@Override
				public void close() {
					// No-Op
				}
			};
		}

		@Override
		protected String getType() {
			return "InputStream Partial Read";
		}

		@Override
		protected long predictSize() {
			return FALLBACK_SIZE;
		}

		@Override
		protected InputStream getIn() {
			return in;
		}

	}

	/**
	 * A wrapper class for reading the contents of a file on disk.
	 */
	protected static class DiskReadOperation extends ReadOperation {

		private final Path path;

		DiskReadOperation(Path path) {
			this.path = path;
		}

		@Override
		protected String getType() {
			return "Disk Read";
		}

		@Override
		protected long predictSize() {
			try {
				return Files.size(path);
			} catch (IOException e) {
				return FALLBACK_SIZE;
			}
		}

		@Override
		protected InputStream getIn() throws IOException {
			return Files.newInputStream(path, StandardOpenOption.READ);
		}

	}

	public class AsyncReadTask {
		private final CompletableFuture<Void> future;

		public <T> AsyncReadTask(Supplier<Result<T>> parser, Consumer<T> callback) {
			future = new CompletableFuture<>();

			Throwable trigger = new Throwable("Invoker stack trace:");
			ThreadFactory.run("GrieferUtils IO - " + getType(), MIN_PRIORITY,
				() -> parser.get().match(v -> {
					callback.accept(v);
					future.complete(null);
				}, e -> {
					trigger.initCause(e);
					future.completeExceptionally(trigger);
				}));
		}

		/**
		 * Runs the task, logging any errors into stdout.
		 */
		public void failLog() {
			future.exceptionally(e -> {
				e.printStackTrace(System.out);
				return null;
			});
		}

		/**
		 * Runs the task, ignoring any errors.
		 */
		public void failSilently() {
			// NO-OP
		}

		/**
		 * Fails silently or logs any errors into stdout.
		 */
		public void failSilentlyIf(boolean silent) {
			if (silent)
				failLog();
			else
				failSilently();
		}

		public void orElse(Runnable fallback) {
			future.exceptionally(ignored -> {
				fallback.run();
				return null;
			});
		}
	}

}
