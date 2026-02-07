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
     */
    List<AuthEntry> loadEntries(byte[] key, EncryptionMethod method) throws Exception;

    /**
     * Encrypt and persist all entries (replaces existing data).
     */
    void saveEntries(byte[] key, EncryptionMethod method, List<AuthEntry> entries) throws Exception;

    /**
     * True if any vault data exists (so we know vault was created).
     */
    boolean hasData() throws Exception;
}
