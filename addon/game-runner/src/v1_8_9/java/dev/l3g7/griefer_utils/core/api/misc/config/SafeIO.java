package dev.l3g7.griefer_utils.core.api.misc.config;

import dev.l3g7.griefer_utils.core.api.util.IOUtil;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * A utility class for concurrent-safe I/O operations.
 * TODO: Move to api.util
 *
 * @see IOUtil
 */
public class SafeIO {

	/**
	 * Atomically writes data to the destination.
	 */
	public static void write(Path destination, byte[] data) throws IOException {
		Path tempFile = Files.createTempFile("GrieferUtils-atomic", ".json");
		Files.write(tempFile, data, StandardOpenOption.CREATE);
		forceMove(tempFile, destination);
	}

	/**
	 * Atomically moves source to destination, replacing existing files.
	 */
	public static void forceMove(Path source, Path destination) throws IOException {
		try {
			Files.move(source, destination, REPLACE_EXISTING, ATOMIC_MOVE);
		} catch (AtomicMoveNotSupportedException e) {
			Files.move(source, destination, REPLACE_EXISTING);
		}
	}

	/**
	 * Atomically moves source to destination, aborting if the file already exists.
	 *
	 * @return true if the file was moved, false if it already exists.
	 */
	public static boolean tryMove(Path source, Path destination) throws IOException {
		try {
			try {
				Files.move(source, destination, ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(source, destination);
			}
			return true;
		} catch (FileAlreadyExistsException e) {
			return false;
		}
	}

}
