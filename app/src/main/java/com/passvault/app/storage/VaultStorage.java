package com.passvault.app.storage;

import com.passvault.app.data.AuthEntry;
import com.passvault.app.data.EncryptionMethod;

import java.util.List;

/**
 * Persistence for vault entries. Implementations: file (.dat) or SQL database.
 * Encrypt/decrypt is done by the implementation using the provided key.
 */
public interface VaultStorage {

    /**
     * Load and decrypt all entries. Returns empty list if none.
     * @param includeHistory when false, history is left empty (faster load for list view).
     */
    List<AuthEntry> loadEntries(byte[] key, EncryptionMethod method, boolean includeHistory) throws Exception;

    /**
     * Load a single entry by id with history (e.g. for More Info or Edit).
     */
    AuthEntry getEntryWithHistory(byte[] key, EncryptionMethod method, String entryId) throws Exception;

    /**
     * Encrypt and persist all entries (replaces existing data).
     */
    void saveEntries(byte[] key, EncryptionMethod method, List<AuthEntry> entries) throws Exception;

    /**
     * True if any vault data exists (so we know vault was created).
     */
    boolean hasData() throws Exception;

    /**
     * Permanently delete all vault data (used when "real wipe" is enabled after too many wrong passwords).
     */
    void wipe() throws Exception;
}
