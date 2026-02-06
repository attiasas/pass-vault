package com.passvault.app.crypto;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Derives encryption key and verification hash from master password + salt.
 */
public final class KeyDerivation {

    private static final int SALT_BYTES = 32;
    private static final int KEY_ITERATIONS = 120000;
    private static final int KEY_BIT_LENGTH = 256;
    private static final int HASH_ITERATIONS = 120000;
    private static final int HASH_BIT_LENGTH = 256;

    private KeyDerivation() {
    }

    public static byte[] generateSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[SALT_BYTES];
        sr.nextBytes(salt);
        return salt;
    }

    public static byte[] deriveKey(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password, salt, KEY_ITERATIONS, KEY_BIT_LENGTH);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return f.generateSecret(spec).getEncoded();
    }

    public static String deriveHashForVerification(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password, salt, HASH_ITERATIONS, HASH_BIT_LENGTH);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = f.generateSecret(spec).getEncoded();
        return Base64.encodeToString(hash, Base64.NO_WRAP);
    }

    public static boolean verifyPassword(char[] password, byte[] salt, String storedHash) {
        try {
            String computed = deriveHashForVerification(password, salt);
            return computed != null && computed.equals(storedHash);
        } catch (Exception e) {
            return false;
        }
    }
}
