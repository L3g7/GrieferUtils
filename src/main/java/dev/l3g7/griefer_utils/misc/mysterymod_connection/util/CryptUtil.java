package dev.l3g7.griefer_utils.misc.mysterymod_connection.util;

import javax.crypto.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class CryptUtil {

    public static SecretKey generateKey() {
        try {
            KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
            keygenerator.init(128);
            return keygenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
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
            X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(encodedKey);
            KeyFactory keyfactory = KeyFactory.getInstance("RSA");
            return keyfactory.generatePublic(encodedKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encryptData(Key key, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

}
