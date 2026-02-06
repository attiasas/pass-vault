package com.passvault.app;

import android.app.Application;

import com.passvault.app.storage.VaultRepository;

/**
 * Application instance; holds vault repository for the session.
 */
public class PassVaultApp extends Application {

    private VaultRepository vaultRepository;

    public VaultRepository getVaultRepository() {
        if (vaultRepository == null) {
            vaultRepository = new VaultRepository(this);
        }
        return vaultRepository;
    }
}
