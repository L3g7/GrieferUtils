package dev.l3g7.griefer_utils.core.api.util.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.util.io.ReadOperation.DiskReadOperation;
import dev.l3g7.griefer_utils.core.api.util.io.ReadOperation.InputStreamReadOperation;
import dev.l3g7.griefer_utils.core.api.util.io.WriteOperation.DiskWriteOperation;
import dev.l3g7.griefer_utils.core.api.util.io.WriteOperation.OutputStreamWriteOperation;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * A utility class for simplified I/O operations.
 */
public class IO {

	public static final Gson GSON = new Gson();
	public static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * @return A wrapper class for reading the contents of the given input stream.
	 */
	public static ReadOperation read(InputStream in) {
		return new InputStreamReadOperation(in);
	}

	/**
	 * @return A wrapper class for reading the contents of the given byte array.
	 */
	public static ReadOperation read(byte[] in) {
		return new InputStreamReadOperation(new ByteArrayInputStream(in));
	}

	/**
	 * @return A wrapper class for reading the contents of the given file on disk.
	 */
	public static ReadOperation read(Path path) {
		return new DiskReadOperation(path);
	}

	/**
	 * @return A wrapper class for reading the contents of the given URL using HTTP GET.
	 */
	public static HttpGetOperation read(String url) {
		return new HttpGetOperation(url);
	}

	/**
	 * @return A wrapper class for writing to the given output stream.
	 */
	public static WriteOperation write(OutputStream in) {
		return new OutputStreamWriteOperation(in);
	}

	/**
	 * @return A wrapper class for writing to the given file on disk.
	 */
	public static WriteOperation write(Path path) {
		return new DiskWriteOperation(path);
	}

	/**
	 * @return The user agent to be used for HTTP requests made by GrieferUtils.
	 */
	public static String getUserAgent() {
		return "GrieferUtils v" + LabyBridge.labyBridge.addonVersion() + " | github.com/L3g7/GrieferUtils";
	}

}
