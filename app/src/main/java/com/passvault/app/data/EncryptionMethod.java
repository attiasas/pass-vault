package com.passvault.app.data;

/**
 * Supported encryption methods for vault data.
 */
public enum EncryptionMethod {
    AES_256_GCM("AES-256-GCM"),
    AES_256_CBC("AES-256-CBC");

    private final String displayName;

    EncryptionMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
