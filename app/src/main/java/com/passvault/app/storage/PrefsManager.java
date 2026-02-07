package com.passvault.app.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.passvault.app.data.EncryptionMethod;

/**
 * Stores salt, master password hash, encryption method, and storage type.
 */
public class PrefsManager {

    private static final String PREFS_NAME = "passvault_prefs";
    private static final String KEY_SALT = "salt";
    private static final String KEY_MASTER_HASH = "master_hash";
    private static final String KEY_ENCRYPTION_METHOD = "encryption_method";
    private static final String KEY_VAULT_EXISTS = "vault_exists";
    private static final String KEY_STORAGE_TYPE = "storage_type";
    private static final String KEY_REUSE_CHECK_COUNT = "reuse_check_count";
    private static final String KEY_ENFORCE_REUSE_CHECK = "enforce_reuse_check";
    private static final String KEY_WIPE_AFTER_ATTEMPTS = "wipe_after_attempts";
    private static final String KEY_PRANK_ONLY = "prank_only";

    private static final int DEFAULT_REUSE_CHECK_COUNT = 3;
    private static final int DEFAULT_WIPE_AFTER_ATTEMPTS = 2;

    private final SharedPreferences prefs;

    public PrefsManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public byte[] getSalt() {
        String b64 = prefs.getString(KEY_SALT, null);
        if (b64 == null) return null;
        return Base64.decode(b64, Base64.NO_WRAP);
    }

    public void setSalt(byte[] salt) {
        prefs.edit()
                .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
                .putBoolean(KEY_VAULT_EXISTS, true)
                .apply();
    }

    public String getMasterHash() {
        return prefs.getString(KEY_MASTER_HASH, null);
    }

    public void setMasterHash(String hash) {
        prefs.edit().putString(KEY_MASTER_HASH, hash).apply();
    }

    public EncryptionMethod getEncryptionMethod() {
        String name = prefs.getString(KEY_ENCRYPTION_METHOD, EncryptionMethod.AES_256_GCM.name());
        try {
            return EncryptionMethod.valueOf(name);
        } catch (Exception e) {
            return EncryptionMethod.AES_256_GCM;
        }
    }

    public void setEncryptionMethod(EncryptionMethod method) {
        prefs.edit().putString(KEY_ENCRYPTION_METHOD, method.name()).apply();
    }

    public StorageType getStorageType() {
        String name = prefs.getString(KEY_STORAGE_TYPE, StorageType.FILE.name());
        try {
            return StorageType.valueOf(name);
        } catch (Exception e) {
            return StorageType.FILE;
        }
    }

    public void setStorageType(StorageType type) {
        prefs.edit().putString(KEY_STORAGE_TYPE, type.name()).apply();
    }

    public boolean isVaultCreated() {
        return prefs.getBoolean(KEY_VAULT_EXISTS, false);
    }

    public void clearVaultFlag() {
        prefs.edit().remove(KEY_SALT).remove(KEY_MASTER_HASH).remove(KEY_VAULT_EXISTS).apply();
    }

    /** Number of recent passwords to check for reuse (default 3). */
    public int getReuseCheckCount() {
        int v = prefs.getInt(KEY_REUSE_CHECK_COUNT, DEFAULT_REUSE_CHECK_COUNT);
        return v < 1 ? 1 : (v > 50 ? 50 : v);
    }

    public void setReuseCheckCount(int count) {
        int v = count < 1 ? 1 : (count > 50 ? 50 : count);
        prefs.edit().putInt(KEY_REUSE_CHECK_COUNT, v).apply();
    }

    /** When true, save is blocked if the new password was used in the last X. */
    public boolean getEnforceReuseCheck() {
        return prefs.getBoolean(KEY_ENFORCE_REUSE_CHECK, true);
    }

    public void setEnforceReuseCheck(boolean enforce) {
        prefs.edit().putBoolean(KEY_ENFORCE_REUSE_CHECK, enforce).apply();
    }

    /** Failed attempts before wipe (when real wipe is on). Default 2. */
    public int getWipeAfterAttempts() {
        int v = prefs.getInt(KEY_WIPE_AFTER_ATTEMPTS, DEFAULT_WIPE_AFTER_ATTEMPTS);
        return v < 1 ? 1 : (v > 10 ? 10 : v);
    }

    public void setWipeAfterAttempts(int count) {
        int v = count < 1 ? 1 : (count > 10 ? 10 : count);
        prefs.edit().putInt(KEY_WIPE_AFTER_ATTEMPTS, v).apply();
    }

    /** When true, wrong-password messages are prank only (no real wipe). Default true. */
    public boolean getPrankOnly() {
        return prefs.getBoolean(KEY_PRANK_ONLY, true);
    }

    public void setPrankOnly(boolean prankOnly) {
        prefs.edit().putBoolean(KEY_PRANK_ONLY, prankOnly).apply();
    }
}
