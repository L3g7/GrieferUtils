/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.util;

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
