package com.passvault.app.storage;

/**
 * Where vault data is stored: single file (.dat) or SQLite database.
 */
public enum StorageType {
    FILE("File (.dat)"),
    SQL("SQL database");

    private final String displayName;

    StorageType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
