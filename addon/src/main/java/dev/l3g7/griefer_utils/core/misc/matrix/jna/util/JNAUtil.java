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

package dev.l3g7.griefer_utils.core.misc.matrix.jna.util;

import com.sun.jna.Memory;
import dev.l3g7.griefer_utils.core.misc.functions.Supplier;

import java.security.SecureRandom;

public class JNAUtil {

	private static final SecureRandom SECURE_RANDOM = ((Supplier<SecureRandom>) SecureRandom::getInstanceStrong).get();

	/**
	 * Allocates space in the native heap.
	 */
	public static Memory malloc(Number size) {
		return new Memory(size.longValue());
	}

	/**
	 * Allocates space in the native heap and copies the given bytes into it.
	 */
	public static Buffer malloc(byte[] buffer) {
		if (buffer.length == 0)
			return new Buffer();

		Memory memory = malloc(buffer.length);
		memory.write(0, buffer, 0, buffer.length);
		return new Buffer(memory);
	}

	/**
	 * Allocates space in the native heap and fills it with cryptographically secure random numbers.
	 */
	public static Buffer random(Number size) {
		if (size.intValue() <= 0)
			return new Buffer();

		byte[] random = new byte[size.intValue()];
		SECURE_RANDOM.nextBytes(random);
		return malloc(random);
	}

	/**
	 * Reads a string from the given memory.
	 */
	public static String getString(Memory memory) {
		return new String(memory.getByteArray(0, (int) memory.size()));
	}

}