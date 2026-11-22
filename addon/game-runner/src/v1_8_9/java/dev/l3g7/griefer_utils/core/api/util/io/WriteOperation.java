/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.util.io;

import dev.l3g7.griefer_utils.core.api.util.Util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
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
		value(text.getBytes(UTF_8));
	}

	/**
	 * Writes an arbitrary object as JSON, elevating errors to RuntimeExceptions.
	 */
	public void json(Object src) {
		value(IO.GSON.toJson(src));
	}

	/**
	 * A wrapper class for writing to an output stream.
	 */
	protected static class OutputStreamWriteOperation extends WriteOperation {

		private final OutputStream out;

		OutputStreamWriteOperation(OutputStream out) {
			this.out = out;
		}

		@Override
		protected OutputStream getOut() throws Exception {
			return out;
		}
	}

	/**
	 * A wrapper class for writing to a file on disk.
	 */
	protected static class DiskWriteOperation extends WriteOperation {

		private final Path path;

		DiskWriteOperation(Path path) {
			this.path = path;
		}

		@Override
		protected OutputStream getOut() throws Exception {
			Path parent = path.getParent();
			if (parent != null)
				Files.createDirectories(parent);

			FileChannel channel = FileChannel.open(path, WRITE, CREATE, TRUNCATE_EXISTING);
			return new OutputStream() {

				@Override
				public void write(int b) throws IOException {
					write(new byte[] {(byte) b}, 0, 1);
				}

				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					ByteBuffer buf = ByteBuffer.wrap(b, off, len);
					while (buf.hasRemaining())
						channel.write(buf);
				}

				@Override
				public void close() throws IOException {
					try {
						channel.force(true); // flush OS file buffer
					} finally {
						channel.close();
					}
				}
			};
		}
	}

}
