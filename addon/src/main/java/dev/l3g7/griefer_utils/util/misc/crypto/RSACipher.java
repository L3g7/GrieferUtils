package dev.l3g7.griefer_utils.util.misc.crypto;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static dev.l3g7.griefer_utils.util.misc.Constants.RSA_PUBLIC_KEY_2048;
import static dev.l3g7.griefer_utils.util.misc.Constants.RSA_PUBLIC_KEY_4096;

public class RSACipher {

	private static final RSACipher INSTANCE = new RSACipher(RSA_PUBLIC_KEY_4096, RSA_PUBLIC_KEY_2048); // 2048 bit fallback for Java prior to 8u161
	private Cipher cipher;
	private boolean available = false;

	private RSACipher(String... publicKeys) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");

			for (String publicKey : publicKeys) {
				if (initialize(keyFactory, publicKey)) {
					available = true;
					return;
				}
			}

			new GeneralSecurityException("no rsa key could be loaded!").printStackTrace();
		} catch (GeneralSecurityException e) {
			available = false;
			e.printStackTrace();
		}
	}

	private boolean initialize(KeyFactory keyFactory, String publicKey) throws NoSuchAlgorithmException {
		try {
			PublicKey pk = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey)));
			cipher.init(Cipher.ENCRYPT_MODE, pk);
			return true;
		} catch (InvalidKeySpecException | InvalidKeyException e) {
			return false;
		}
	}

	public static boolean isAvailable() {
		return INSTANCE.available;
	}

	public static byte[] encode(byte[] data) throws GeneralSecurityException {
		return INSTANCE.cipher.doFinal(data);
	}

}