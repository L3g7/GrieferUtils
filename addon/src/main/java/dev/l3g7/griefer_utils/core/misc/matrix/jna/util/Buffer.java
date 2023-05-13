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
import com.sun.jna.Pointer;

/**
 * A wrapper around {@link Memory} allowing a size of 0.
 *
 * @see JNAUtil
 */
public class Buffer extends Pointer {

	private final size_t size;

	public Buffer(Memory memory) {
		super(Pointer.nativeValue(memory));
		this.size = new size_t(memory.size());
	}

	public Buffer() {
		super(0);
		this.size = size_t.ZERO;
	}

	public size_t size() {
		return size;
	}

}