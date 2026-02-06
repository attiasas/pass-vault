package com.passvault.app.crypto;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.passvault.app.data.EncryptionMethod;

/**
 * Encrypt/decrypt vault payload using AES with the chosen method.
 */
public final class VaultCipher {

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int CBC_IV_LENGTH = 16;
    private static final String AES = "AES";

    private VaultCipher() {
    }

    public static String encrypt(byte[] key, String plainText, EncryptionMethod method) throws Exception {
        if (plainText == null) return null;
        byte[] input = plainText.getBytes(StandardCharsets.UTF_8);
        if (method == EncryptionMethod.AES_256_GCM) {
            return encryptGcm(key, input);
        } else {
            return encryptCbc(key, input);
        }
    }

    public static String decrypt(byte[] key, String cipherBase64, EncryptionMethod method) throws Exception {
        if (cipherBase64 == null) return null;
        byte[] decoded = Base64.decode(cipherBase64, Base64.NO_WRAP);
        if (method == EncryptionMethod.AES_256_GCM) {
            return decryptGcm(key, decoded);
        } else {
            return decryptCbc(key, decoded);
        }
    }

    private static String encryptGcm(byte[] key, byte[] input) throws Exception {
        SecureRandom sr = new SecureRandom();
        byte[] iv = new byte[GCM_IV_LENGTH];
        sr.nextBytes(iv);

        SecretKeySpec keySpec = new SecretKeySpec(key, AES);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        byte[] encrypted = cipher.doFinal(input);

        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return Base64.encodeToString(combined, Base64.NO_WRAP);
    }

    private static String decryptGcm(byte[] key, byte[] combined) throws Exception {
        if (combined.length < GCM_IV_LENGTH) throw new IllegalArgumentException("Invalid cipher");
        byte[] iv = Arrays.copyOfRange(combined, 0, GCM_IV_LENGTH);
        byte[] encrypted = Arrays.copyOfRange(combined, GCM_IV_LENGTH, combined.length);

        SecretKeySpec keySpec = new SecretKeySpec(key, AES);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private static String encryptCbc(byte[] key, byte[] input) throws Exception {
        SecureRandom sr = new SecureRandom();
        byte[] iv = new byte[CBC_IV_LENGTH];
        sr.nextBytes(iv);

        SecretKeySpec keySpec = new SecretKeySpec(key, AES);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(input);

        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return Base64.encodeToString(combined, Base64.NO_WRAP);
    }

    private static String decryptCbc(byte[] key, byte[] combined) throws Exception {
        if (combined.length < CBC_IV_LENGTH) throw new IllegalArgumentException("Invalid cipher");
        byte[] iv = Arrays.copyOfRange(combined, 0, CBC_IV_LENGTH);
        byte[] encrypted = Arrays.copyOfRange(combined, CBC_IV_LENGTH, combined.length);

        SecretKeySpec keySpec = new SecretKeySpec(key, AES);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
