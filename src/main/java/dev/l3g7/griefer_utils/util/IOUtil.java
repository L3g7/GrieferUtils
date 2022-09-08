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

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static final JsonParser JSON_PARSER = new JsonParser();

	public static FileRequest file(File file) {
		return new FileRequest(file);
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

}
