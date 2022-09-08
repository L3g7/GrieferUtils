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

}
