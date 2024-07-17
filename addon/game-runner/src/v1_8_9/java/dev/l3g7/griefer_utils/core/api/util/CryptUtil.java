/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class CryptUtil {

	private static final KeyFactory RSA_KEYFACTORY;
	private static final Cipher AES_CIPHER;
	private static final Cipher RSA_CIPHER;

	static {
		try {
			RSA_KEYFACTORY = KeyFactory.getInstance("RSA");
			AES_CIPHER = Cipher.getInstance("AES/CBC/PKCS5Padding");
			RSA_CIPHER = Cipher.getInstance("RSA");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new RuntimeException(e);
		}
	}

	public static SecretKey generateKey() {
        try {
            KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
            keygenerator.init(128);
            return keygenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

	public static IvParameterSpec generateIv() {
		byte[] iv = new byte[16];
		new SecureRandom().nextBytes(iv);
		return new IvParameterSpec(iv);
	}

    public static byte[] getServerIdHash(String serverId, PublicKey publicKey, SecretKey secretKey) {
        try {
            MessageDigest messagedigest = MessageDigest.getInstance("SHA-1");
            messagedigest.update(serverId.getBytes(StandardCharsets.ISO_8859_1));
            messagedigest.update(secretKey.getEncoded());
            messagedigest.update(publicKey.getEncoded());
            return messagedigest.digest();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicKey decodePublicKey(byte[] encodedKey) {
        try {
            return RSA_KEYFACTORY.generatePublic(new X509EncodedKeySpec(encodedKey));
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

	public static PrivateKey decodePrivateKey(byte[] encodedKey) {
		try {
			return RSA_KEYFACTORY.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] aesDigest(SecretKey key, IvParameterSpec iv, byte[] data, int mode) {
		try {
			AES_CIPHER.init(mode, key, iv);
			return AES_CIPHER.doFinal(data);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

    public static byte[] rsaDigest(Key key, byte[] data, int mode) {
        try {
	        RSA_CIPHER.init(mode, key);
            return RSA_CIPHER.doFinal(data);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

}