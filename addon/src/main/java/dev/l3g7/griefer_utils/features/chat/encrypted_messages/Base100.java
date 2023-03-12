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

package dev.l3g7.griefer_utils.features.chat.encrypted_messages;

import java.util.Arrays;
import java.util.Base64;

/*
 * Copyright 2011 Google Inc.
 * Copyright 2018 Andreas Schildbach
 *
 * https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/base/Base58.java
 */
public class Base100 {

	public static final char[] ALPHABET = "=~+-'`\"#!é%*,.:;?@^/\\_<{([|])}>0123456789AaÄäBbCcDdEeFfGgHhIiJjKkLlMmNnOoÖöPpQqRrSsßTtUuÜüVvWwXxYyZz".toCharArray();
	private static final char ENCODED_ZERO = ALPHABET[0];
	private static final int[] INDEXES = new int[253];
	static {
		Arrays.fill(INDEXES, -1);
		for (int i = 0; i < ALPHABET.length; i++) {
			try {
				INDEXES[ALPHABET[i]] = i;
			} catch (ArrayIndexOutOfBoundsException e) {
				System.err.println(i + " " + ALPHABET[i]);
				throw new RuntimeException(e);
			}
		}
	}

	public static String encode(byte[] input) {
		if (input.length == 0) {
			return "";
		}
		int zeros = 0;
		while (zeros < input.length && input[zeros] == 0) {
			++zeros;
		}
		input = Arrays.copyOf(input, input.length);
		char[] encoded = new char[input.length * 2];
		int outputStart = encoded.length;
		for (int inputStart = zeros; inputStart < input.length; ) {
			encoded[--outputStart] = ALPHABET[divmod(input, inputStart, 256, 58)];
			if (input[inputStart] == 0) {
				++inputStart;
			}
		}
		while (outputStart < encoded.length && encoded[outputStart] == ENCODED_ZERO) {
			++outputStart;
		}
		while (--zeros >= 0) {
			encoded[--outputStart] = ENCODED_ZERO;
		}

		String res = new String(encoded, outputStart, encoded.length - outputStart);
		if (res.contains(" ")) {
			Base64.getEncoder().encodeToString(input);
		}

		return res;
	}

	public static byte[] decode(String input) {
		if (input.length() == 0) {
			return new byte[0];
		}
		byte[] input58 = new byte[input.length()];
		for (int i = 0; i < input.length(); ++i) {
			char c = input.charAt(i);
			int digit = c < 253 ? INDEXES[c] : -1;
			if (digit < 0) {
				throw new IllegalStateException("invalid character '" + c + "'");
			}
			input58[i] = (byte) digit;
		}
		int zeros = 0;
		while (zeros < input58.length && input58[zeros] == 0) {
			++zeros;
		}
		byte[] decoded = new byte[input.length()];
		int outputStart = decoded.length;
		for (int inputStart = zeros; inputStart < input58.length; ) {
			decoded[--outputStart] = divmod(input58, inputStart, 58, 256);
			if (input58[inputStart] == 0) {
				++inputStart;
			}
		}
		while (outputStart < decoded.length && decoded[outputStart] == 0) {
			++outputStart;
		}
		return Arrays.copyOfRange(decoded, outputStart - zeros, decoded.length);
	}

	private static byte divmod(byte[] number, int firstDigit, int base, int divisor) {
		int remainder = 0;
		for (int i = firstDigit; i < number.length; i++) {
			int digit = (int) number[i] & 0xFF;
			int temp = remainder * base + digit;
			number[i] = (byte) (temp / divisor);
			remainder = temp % divisor;
		}
		return (byte) remainder;
	}
}