package com.passvault.app.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.passvault.app.data.EncryptionMethod;

/**
 * Stores salt, master password hash, and chosen encryption method.
 */
public class PrefsManager {

    private static final String PREFS_NAME = "passvault_prefs";
    private static final String KEY_SALT = "salt";
    private static final String KEY_MASTER_HASH = "master_hash";
    private static final String KEY_ENCRYPTION_METHOD = "encryption_method";
    private static final String KEY_VAULT_EXISTS = "vault_exists";

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

    public boolean isVaultCreated() {
        return prefs.getBoolean(KEY_VAULT_EXISTS, false);
    }

    public void clearVaultFlag() {
        prefs.edit().remove(KEY_SALT).remove(KEY_MASTER_HASH).remove(KEY_VAULT_EXISTS).apply();
    }
}
