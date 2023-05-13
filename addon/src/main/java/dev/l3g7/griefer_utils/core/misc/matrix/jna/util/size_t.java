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

import com.sun.jna.IntegerType;
import com.sun.jna.Native;

/**
 * A Java class representing the C type {@code size_t}.
 */
public class size_t extends IntegerType {

	public static final size_t ZERO = new size_t();

	private static final long serialVersionUID = 1L;

	public size_t() {
		this(0);
	}

	public size_t(long value) {
		super(Native.SIZE_T_SIZE, value, true);
	}

}