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

package dev.l3g7.griefer_utils.features.uncategorized.settings.debug.log;

import dev.l3g7.griefer_utils.core.reflection.Reflection;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class LogHook {

	public static final Path FILE = Paths.get(System.getProperty("java.io.tmpdir"), "griefer_utils_debug_log.txt");

	public static void hook() {
		try {
			OutputStream sOut = findLowestStream();
			OutputStream out = Reflection.get(sOut, "out");

			Files.deleteIfExists(FILE);

			Reflection.set(sOut, new OutputStream() {
				public void write(int b) {
					try {
						out.write(b);
						Files.write(FILE, new byte[] {((byte) b)}, APPEND, CREATE);
					} catch (Throwable t) {
						t.printStackTrace(new PrintStream(sOut));
						throw new RuntimeException(t);
					}
				}
			}, "out");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static OutputStream findLowestStream() {
		OutputStream sOut = System.out;
		while (true) {
			OutputStream child = Reflection.get(sOut, "out");
			if (!(child instanceof FilterOutputStream))
				break;

			sOut = child;
		}
		return sOut;
	}

}