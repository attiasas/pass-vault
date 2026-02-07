package com.passvault.app.storage;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.passvault.app.crypto.VaultCipher;
import com.passvault.app.data.AuthEntry;
import com.passvault.app.data.EncryptionMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores all entries as a single encrypted JSON file (vault.dat).
 */
public class FileVaultStorage implements VaultStorage {

    private static final String VAULT_FILE = "vault.dat";

    private final Context context;
    private final Gson gson = new Gson();

    public FileVaultStorage(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public List<AuthEntry> loadEntries(byte[] key, EncryptionMethod method) throws Exception {
        File file = new File(context.getFilesDir(), VAULT_FILE);
        if (!file.exists() || file.length() == 0) return new ArrayList<>();
        byte[] raw;
        try (FileInputStream fis = new FileInputStream(file)) {
            raw = new byte[(int) file.length()];
            int n = fis.read(raw);
            if (n != raw.length) throw new IOException("Short read");
        }
        String json = VaultCipher.decrypt(key, new String(raw, StandardCharsets.UTF_8), method);
        if (json == null || json.isEmpty()) return new ArrayList<>();
        List<AuthEntry> list = gson.fromJson(json, new TypeToken<List<AuthEntry>>() {}.getType());
        return list != null ? list : new ArrayList<>();
    }

    @Override
    public void saveEntries(byte[] key, EncryptionMethod method, List<AuthEntry> entries) throws Exception {
        String json = gson.toJson(entries != null ? entries : new ArrayList<AuthEntry>());
        String encrypted = VaultCipher.encrypt(key, json, method);
        File file = new File(context.getFilesDir(), VAULT_FILE);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(encrypted.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public boolean hasData() throws Exception {
        File file = new File(context.getFilesDir(), VAULT_FILE);
        return file.exists() && file.length() > 0;
    }
}
