package com.passvault.app.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.passvault.app.crypto.VaultCipher;
import com.passvault.app.data.AuthEntry;
import com.passvault.app.data.EncryptionMethod;
import com.passvault.app.data.EntryHistoryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores entries in SQLite with proper schema. Sensitive columns (password, history) are encrypted.
 */
public class SqlVaultStorage implements VaultStorage {

    private static final String DB_NAME = "passvault.db";
    private static final int VERSION = 1;

    private static final String TABLE_ENTRIES = "entries";
    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD_ENCRYPTED = "password_encrypted";
    private static final String COL_CREATED_AT = "created_at";
    private static final String COL_UPDATED_AT = "updated_at";
    private static final String COL_HISTORY_ENCRYPTED = "history_encrypted";

    private final Context context;
    private final Gson gson = new Gson();

    public SqlVaultStorage(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public List<AuthEntry> loadEntries(byte[] key, EncryptionMethod method) throws Exception {
        SqlHelper helper = new SqlHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        List<AuthEntry> result = new ArrayList<>();
        try (Cursor c = db.query(TABLE_ENTRIES, null, null, null, null, null, COL_CREATED_AT + " ASC")) {
            int idxId = c.getColumnIndexOrThrow(COL_ID);
            int idxTitle = c.getColumnIndexOrThrow(COL_TITLE);
            int idxUsername = c.getColumnIndexOrThrow(COL_USERNAME);
            int idxPassword = c.getColumnIndexOrThrow(COL_PASSWORD_ENCRYPTED);
            int idxCreated = c.getColumnIndexOrThrow(COL_CREATED_AT);
            int idxUpdated = c.getColumnIndexOrThrow(COL_UPDATED_AT);
            int idxHistory = c.getColumnIndexOrThrow(COL_HISTORY_ENCRYPTED);
            while (c.moveToNext()) {
                AuthEntry e = new AuthEntry();
                e.setId(c.getString(idxId));
                e.setTitle(c.getString(idxTitle));
                e.setUsername(c.getString(idxUsername));
                String passEnc = c.getString(idxPassword);
                e.setPasswordOrToken(passEnc != null && !passEnc.isEmpty()
                        ? VaultCipher.decrypt(key, passEnc, method) : "");
                e.setCreatedAt(c.getLong(idxCreated));
                e.setUpdatedAt(c.getLong(idxUpdated));
                String histEnc = c.getString(idxHistory);
                if (histEnc != null && !histEnc.isEmpty()) {
                    String histJson = VaultCipher.decrypt(key, histEnc, method);
                    if (histJson != null && !histJson.isEmpty()) {
                        List<EntryHistoryItem> history = gson.fromJson(histJson,
                                new TypeToken<List<EntryHistoryItem>>() {}.getType());
                        e.setHistory(history != null ? history : new ArrayList<>());
                    }
                }
                result.add(e);
            }
        }
        return result;
    }

    @Override
    public void saveEntries(byte[] key, EncryptionMethod method, List<AuthEntry> entries) throws Exception {
        SqlHelper helper = new SqlHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_ENTRIES, null, null);
            if (entries != null) {
                for (AuthEntry e : entries) {
                    ContentValues cv = new ContentValues();
                    cv.put(COL_ID, e.getId());
                    cv.put(COL_TITLE, e.getTitle() != null ? e.getTitle() : "");
                    cv.put(COL_USERNAME, e.getUsername() != null ? e.getUsername() : "");
                    String pass = e.getPasswordOrToken();
                    cv.put(COL_PASSWORD_ENCRYPTED, pass != null && !pass.isEmpty()
                            ? VaultCipher.encrypt(key, pass, method) : "");
                    cv.put(COL_CREATED_AT, e.getCreatedAt());
                    cv.put(COL_UPDATED_AT, e.getUpdatedAt());
                    String historyJson = gson.toJson(e.getHistory());
                    cv.put(COL_HISTORY_ENCRYPTED, historyJson != null && !historyJson.isEmpty()
                            ? VaultCipher.encrypt(key, historyJson, method) : "");
                    db.insert(TABLE_ENTRIES, null, cv);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public boolean hasData() throws Exception {
        SqlHelper helper = new SqlHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query(TABLE_ENTRIES, new String[]{COL_ID}, null, null, null, null, null, "1")) {
            return c.getCount() > 0;
        }
    }

    private static final class SqlHelper extends SQLiteOpenHelper {
        SqlHelper(Context context) {
            super(context, DB_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_ENTRIES + " ("
                    + COL_ID + " TEXT PRIMARY KEY, "
                    + COL_TITLE + " TEXT NOT NULL, "
                    + COL_USERNAME + " TEXT NOT NULL, "
                    + COL_PASSWORD_ENCRYPTED + " TEXT NOT NULL, "
                    + COL_CREATED_AT + " INTEGER NOT NULL, "
                    + COL_UPDATED_AT + " INTEGER NOT NULL, "
                    + COL_HISTORY_ENCRYPTED + " TEXT NOT NULL)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
    }
}
