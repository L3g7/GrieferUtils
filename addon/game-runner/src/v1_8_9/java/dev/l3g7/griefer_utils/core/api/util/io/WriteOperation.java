package dev.l3g7.griefer_utils.core.api.util.io;

import dev.l3g7.griefer_utils.core.api.util.Util;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.*;

public abstract class WriteOperation {

	protected abstract OutputStream getOut() throws Exception;

	/**
	 * Writes a byte array, elevating errors to RuntimeExceptions.
	 */
	public void value(byte[] data) {
		try {
			try (OutputStream out = getOut()) {
				out.write(data);
			}
		} catch (Exception e) {
			throw Util.elevate(e);
		}
	}

	/**
	 * Writes a UTF-8 string, elevating errors to RuntimeExceptions.
	 */
	public void value(String text) {
		value(text.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Writes an arbitrary object as JSON, elevating errors to RuntimeExceptions.
	 */
	public void json(Object src) {
		value(IO.GSON.toJson(src));
	}

	/**
	 * A wrapper class for writing the contents of a file on disk.
	 */
	protected static class DiskWriteOperation extends WriteOperation {

		private final Path path;

		DiskWriteOperation(Path path) {
			this.path = path;
		}

		@Override
		protected OutputStream getOut() throws Exception {
			return Files.newOutputStream(path, WRITE, CREATE, TRUNCATE_EXISTING);
		}
	}


}
