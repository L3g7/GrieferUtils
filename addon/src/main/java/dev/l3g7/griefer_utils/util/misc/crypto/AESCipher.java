package dev.l3g7.griefer_utils.util.misc.crypto;

import dev.l3g7.griefer_utils.util.ArrayUtil;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;

public class AESCipher {

	private SecretKey key;
	private byte[] keyIvBundle;
	private Cipher cipher;
	private boolean available;

	private AESCipher(int... keySizes) {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			for (int keySize : keySizes) {
				if (initialize(keyGenerator, keySize)) {
					available = true;
					break;
				}
			}

			if (available)
				keyIvBundle = ArrayUtil.merge(key.getEncoded(), cipher.getIV());
			else
				new GeneralSecurityException("no aes key could be loaded!").printStackTrace();
		} catch (GeneralSecurityException e) {
			available = false;
			e.printStackTrace();
		}
	}

	public static AESCipher generateNew() {
		return new AESCipher(256, 128); // 128 bit fallback for Java prior to 8u161
	}

	private boolean initialize(KeyGenerator keyGenerator, int keySize) {
		try {
			keyGenerator.init(keySize);
			key = keyGenerator.generateKey();
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return true;
		} catch (InvalidKeyException e) {
			return false;
		}
	}

	public boolean isAvailable() {
		return available;
	}

	public byte[] getKeyIvBundle() {
		return keyIvBundle;
	}

	public byte[] encode(byte[] data) throws GeneralSecurityException {
		return cipher.doFinal(data);
	}

}