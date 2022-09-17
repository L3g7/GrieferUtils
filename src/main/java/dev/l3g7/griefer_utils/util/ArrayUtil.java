/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2022 L3g7
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

package dev.l3g7.griefer_utils.util;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A utility class for array stuff.
 */
public class ArrayUtil {

	/**
	 * Checks if two arrays are equal using a custom function.
	 */
	public static <A, B> boolean equals(A[] a, B[] b, BiFunction<A, B, Boolean> func) {
		if(a.length != b.length)
			return false;

		for(int i = 0; i < a.length; i++)
			if(!func.apply(a[i], b[i]))
				return false;

		return true;
	}

	/**
	 * Returns a string representation of an array using a custom function.
	 */
	public static <T> String toString(T[] array, Function<T, String> func, String delimiter) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			if(i != 0)
				s.append(delimiter);
			s.append(func.apply(array[i]));
		}
		return s.toString();
	}

	public static <T> T last(T[] array) {
		if (array.length == 0)
			return null;

		return array[array.length - 1];
	}
}
