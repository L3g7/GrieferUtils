/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 *
 * Copyright 2020-2024 L3g7
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

package dev.l3g7.griefer_utils.api.util;

/**
 * A utility class for String stuff.
 */
public class StringUtil {

	/**
	 * Converts a String from camel case to lower underscore case.
	 */
	public static String convertCasing(String str) {
		return (str.charAt(0) + str.substring(1).replaceAll("([A-Z])", "_$1")).toLowerCase();
	}

	public static boolean isNumeric(final String cs) {
		return cs.chars().allMatch(Character::isDigit);
	}

	/**
	 * @return <code>null</code> if an error occurred.
	 */
	public static byte[] decodeHex(String hex) {
		char[] chars = hex.toCharArray();
		int len = chars.length;

		if (len % 2 == 1)
			return null;

		byte[] out = new byte[len / 2];

		for (int i = 0; i < len; i += 2) {
			int first = Character.digit(chars[i], 16);
			int second = Character.digit(chars[i + 1], 16);
			if (first == -1 || second == -1)
				return null;

			out[i / 2] = (byte) ((first << 4 | second) & 0xFF);
		}

		return out;
	}

}
