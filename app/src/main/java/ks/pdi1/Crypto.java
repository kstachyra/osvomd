package ks.pdi1;

import static ks.pdi1.Constants.*;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public class Crypto
{
    public static SecretKey generateKey(String androidId, String apkConstant) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        char[] androidIdBytes = androidId.toCharArray();
        byte[] apkConstantBytes = apkConstant.getBytes(StandardCharsets.UTF_8);

        final int iterations = CRYPTO_ITERATIONS;
        final int outputKeyLength = CRYPTO_KEY_LENGTH;
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(androidIdBytes, apkConstantBytes, iterations, outputKeyLength);
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
        return secretKey;
    }

    public static byte[] encrypt(SecretKey secretKey, byte[] data) throws Exception
    {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(data);
        return encrypted;
    }

    public static byte[] decrypt(SecretKey secretKey, byte[] encrypted) throws Exception
    {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }
}
