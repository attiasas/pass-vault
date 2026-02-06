package com.passvault.app.util;

import com.google.gson.Gson;
import com.passvault.app.data.AuthEntry;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * Export/import vault entries as JSON (no history).
 */
public final class ExportImport {

    private static final Gson GSON = new Gson();

    private ExportImport() {
    }

    /** Export entries without history (current data only). */
    public static String exportToJson(List<AuthEntry> entries) {
        List<AuthEntry> copy = new java.util.ArrayList<>();
        for (AuthEntry e : entries) {
            AuthEntry c = new AuthEntry();
            c.setId(e.getId());
            c.setTitle(e.getTitle());
            c.setUsername(e.getUsername());
            c.setPasswordOrToken(e.getPasswordOrToken());
            c.setCreatedAt(e.getCreatedAt());
            c.setUpdatedAt(e.getUpdatedAt());
            c.setHistory(new java.util.ArrayList<>());
            copy.add(c);
        }
        ExportData data = new ExportData();
        data.version = 1;
        data.entries = copy;
        return GSON.toJson(data);
    }

    public static List<AuthEntry> importFromJson(String json) {
        ExportData data = GSON.fromJson(json, ExportData.class);
        if (data == null || data.entries == null) return java.util.Collections.emptyList();
        return data.entries;
    }

    public static void writeToStream(String json, OutputStream out) throws java.io.IOException {
        try (OutputStreamWriter w = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            w.write(json);
        }
    }

    public static String readFromStream(InputStream in) throws java.io.IOException {
        try (Scanner s = new Scanner(in, StandardCharsets.UTF_8.name()).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

    @SuppressWarnings("unused")
    public static class ExportData {
        public int version;
        public List<AuthEntry> entries;
    }
}
