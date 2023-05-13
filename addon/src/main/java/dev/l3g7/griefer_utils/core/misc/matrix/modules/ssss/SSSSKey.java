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

package dev.l3g7.griefer_utils.core.misc.matrix.modules.ssss;


import at.favre.lib.hkdf.HKDF;
import dev.l3g7.griefer_utils.core.misc.Base58;
import dev.l3g7.griefer_utils.core.misc.Watchable;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Secure Secret Storage and Sharing.
 * <p>
 * <a href="https://spec.matrix.org/v1.6/client-server-api/#secrets">Matrix spec</a>
 */
public class SSSSKey extends Watchable<byte[]> {

	public SSSSKey() {}

	/**
	 * Extracts the raw key from a string representation.
	 * <p>
	 * <a href="https://spec.matrix.org/v1.6/client-server-api/#key-representation">Matrix spec</a>
	 */
	public void init(String securityPhrase) {
		byte[] key = Base58.decode(securityPhrase.replaceAll(" ", ""));

		// Check length and prefix
		if (key.length != 35)
			throw new IllegalStateException("invalid phrase length");

		if (key[0] != (byte) 0x8b || key[1] != 1)
			throw new IllegalStateException("invalid prefix");

		// Check parity byte
		byte parityByte = 0;
		for (int i = 0; i < 34; i++)
			parityByte ^= key[i];

		if (parityByte != key[34])
			throw new IllegalStateException("invalid parity byte");

		// Extract raw key
		byte[] rawKey = new byte[32];
		System.arraycopy(key, 2, rawKey, 0, 32);
		set(rawKey);
	}

	public void init(byte[] rawKey) {
		set(rawKey);
	}

	/**
	 * Represents a raw key as a string.
	 * <p>
	 * <a href="https://spec.matrix.org/v1.6/client-server-api/#key-representation">Matrix spec</a>
	 */
	public String toString() {
		if (!isSet())
			return "<Uninitialized>";

		// Add prefix
		byte[] key = new byte[35];
		key[0] = (byte) 0x8b;
		key[1] = 1;

		// Add key
		System.arraycopy(get(), 0, key, 2, 32);

		// Add parity byte
		byte parityByte = 0;
		for (int i = 0; i < 34; i++)
			parityByte ^= key[i];
		key[34] = parityByte;

		// Format into groups of 4
		StringBuilder encodedKey = new StringBuilder(Base58.encode(key));
		for (int i = 4; i < encodedKey.length(); i += 5)
			encodedKey.insert(i, ' ');

		return encodedKey.toString();
	}

	/**
	 * Decrypts a ciphertext encrypted using the {@code m.secret_storage.v1.aes-hmac-sha2} algorithm.
	 * <p>
	 * <a href="https://spec.matrix.org/v1.6/client-server-api/#msecret_storagev1aes-hmac-sha2">Matrix spec</a>
	 */
	public byte[] decrypt(String secretName, AesHmacSha2EncryptedData data) throws GeneralSecurityException {
		return decrypt(secretName, Base64.getDecoder().decode(data.iv), Base64.getDecoder().decode(data.ciphertext), Base64.getDecoder().decode(data.mac));
	}

	/**
	 * Decrypts a ciphertext encrypted using the {@code m.secret_storage.v1.aes-hmac-sha2} algorithm.
	 * <p>
	 * <a href="https://spec.matrix.org/v1.6/client-server-api/#msecret_storagev1aes-hmac-sha2">Matrix spec</a>
	 * @param secretName the name of the secret - i.e., the "event type" stored in the account data
	 */
	public byte[] decrypt(String secretName, byte[] iv, byte[] ciphertext, byte[] mac) throws GeneralSecurityException {
		if (!isSet())
			throw new IllegalStateException("Key is not yet initialized!");

		// Generate aes and mac key
		byte[] bytes = HKDF.fromHmacSha256().extractAndExpand(new byte[32], get(), secretName.getBytes(), 64);

		byte[] secretKey = new byte[32];
		byte[] macKey = new byte[32];
		System.arraycopy(bytes, 0, secretKey, 0, 32);
		System.arraycopy(bytes, 32, macKey, 0, 32);

		// Validate MAC
		Mac macGen = Mac.getInstance("HmacSHA256");
		macGen.init(new SecretKeySpec(macKey, "MAC"));
		if (!Arrays.equals(macGen.doFinal(ciphertext), mac))
			throw new GeneralSecurityException("bad MAC!");

		// Decrypt ciphertext
		Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
		c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secretKey, "AES"), new IvParameterSpec(iv));
		return c.doFinal(ciphertext);
	}

}