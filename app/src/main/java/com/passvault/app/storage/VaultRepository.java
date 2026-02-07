package com.passvault.app.storage;

import android.content.Context;

import com.passvault.app.crypto.KeyDerivation;
import com.passvault.app.data.AuthEntry;
import com.passvault.app.data.EncryptionMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Encrypted local storage for auth entries. All data encrypted with master key.
 * Persistence is delegated to {@link VaultStorage} (file or SQL), chosen in settings.
 */
public class VaultRepository {

    private final Context context;
    private final PrefsManager prefs;
    private byte[] currentKey;
    private List<AuthEntry> entriesCache;

    public VaultRepository(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = new PrefsManager(context);
    }

    private VaultStorage getStorage() {
        return prefs.getStorageType() == StorageType.SQL
                ? new SqlVaultStorage(context)
                : new FileVaultStorage(context);
    }

    public void unlock(char[] masterPassword) throws Exception {
        byte[] salt = prefs.getSalt();
        if (salt == null) throw new IllegalStateException("No salt");
        currentKey = KeyDerivation.deriveKey(masterPassword, salt);
        entriesCache = getStorage().loadEntries(currentKey, prefs.getEncryptionMethod());
        if (entriesCache == null) entriesCache = new ArrayList<>();
    }

    public void createVault(char[] masterPassword) throws Exception {
        byte[] salt = KeyDerivation.generateSalt();
        String hash = KeyDerivation.deriveHashForVerification(masterPassword, salt);
        prefs.setSalt(salt);
        prefs.setMasterHash(hash);
        currentKey = KeyDerivation.deriveKey(masterPassword, salt);
        entriesCache = new ArrayList<>();
        saveEntries();
    }

    public boolean verifyPassword(char[] masterPassword) {
        byte[] salt = prefs.getSalt();
        String hash = prefs.getMasterHash();
        return salt != null && hash != null && KeyDerivation.verifyPassword(masterPassword, salt, hash);
    }

    public void changeMasterPassword(char[] oldPassword, char[] newPassword) throws Exception {
        if (currentKey == null) unlock(oldPassword);
        List<AuthEntry> entries = getAllEntries();
        byte[] newSalt = KeyDerivation.generateSalt();
        String newHash = KeyDerivation.deriveHashForVerification(newPassword, newSalt);
        byte[] newKey = KeyDerivation.deriveKey(newPassword, newSalt);
        prefs.setSalt(newSalt);
        prefs.setMasterHash(newHash);
        currentKey = newKey;
        entriesCache = entries;
        saveEntries();
    }

    public void lock() {
        currentKey = null;
        entriesCache = null;
    }

    public boolean isUnlocked() {
        return currentKey != null;
    }

    /** True if a vault has been created (salt/master hash stored). */
    public boolean isVaultCreated() {
        return prefs.isVaultCreated();
    }

    public List<AuthEntry> getAllEntries() {
        if (entriesCache == null) throw new IllegalStateException("Vault locked");
        return new ArrayList<>(entriesCache);
    }

    public void addEntry(AuthEntry entry) {
        if (entriesCache == null) throw new IllegalStateException("Vault locked");
        entriesCache.add(entry);
        saveEntries();
    }

    public void updateEntry(AuthEntry entry) {
        if (entriesCache == null) throw new IllegalStateException("Vault locked");
        for (int i = 0; i < entriesCache.size(); i++) {
            if (entriesCache.get(i).getId().equals(entry.getId())) {
                entriesCache.set(i, entry);
                saveEntries();
                return;
            }
        }
    }

    public void deleteEntry(String id) {
        if (entriesCache == null) throw new IllegalStateException("Vault locked");
        entriesCache.removeIf(e -> e.getId().equals(id));
        saveEntries();
    }

    public AuthEntry getEntryById(String id) {
        if (entriesCache == null) return null;
        for (AuthEntry e : entriesCache) {
            if (e.getId().equals(id)) return e;
        }
        return null;
    }

    public EncryptionMethod getEncryptionMethod() {
        return prefs.getEncryptionMethod();
    }

    public void setEncryptionMethod(EncryptionMethod method) {
        prefs.setEncryptionMethod(method);
    }

    public StorageType getStorageType() {
        return prefs.getStorageType();
    }

    /**
     * Switch storage (file ↔ SQL) and migrate current data. Call when vault is unlocked.
     */
    public void switchStorageType(StorageType newType) {
        if (currentKey == null || entriesCache == null) throw new IllegalStateException("Vault locked");
        if (prefs.getStorageType() == newType) return;
        try {
            prefs.setStorageType(newType);
            getStorage().saveEntries(currentKey, prefs.getEncryptionMethod(), entriesCache);
        } catch (Exception e) {
            throw new RuntimeException("Failed to switch storage", e);
        }
    }

    private void saveEntries() {
        if (currentKey == null || entriesCache == null) return;
        try {
            getStorage().saveEntries(currentKey, prefs.getEncryptionMethod(), entriesCache);
        } catch (Exception e) {
            throw new RuntimeException("Save failed", e);
        }
    }
}
