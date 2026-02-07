package com.passvault.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.passvault.app.PassVaultApp;
import com.passvault.app.R;
import com.passvault.app.databinding.ActivityLoginBinding;
import com.passvault.app.storage.VaultRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private VaultRepository vault;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        vault = ((PassVaultApp) getApplication()).getVaultRepository();

        boolean isNew = !vault.isVaultCreated();
        binding.layoutConfirm.setVisibility(isNew ? View.VISIBLE : View.GONE);
        binding.btnLogin.setText(isNew ? getString(R.string.create_vault) : getString(R.string.login));

        binding.btnLogin.setOnClickListener(v -> {
            char[] pass = getPassword();
            if (pass == null) return;
            if (isNew) {
                char[] confirm = getConfirm();
                if (confirm == null || !java.util.Arrays.equals(pass, confirm)) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                if (!vault.verifyPassword(pass)) {
                    Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            setLoading(true);
            final boolean createVault = isNew;
            executor.execute(() -> {
                Exception error = null;
                try {
                    if (createVault) {
                        vault.createVault(pass);
                    } else {
                        vault.unlock(pass);
                    }
                } catch (Exception e) {
                    error = e;
                }
                runOnUiThread(() -> {
                    setLoading(false);
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        openVault();
                    }
                });
            });
        });
    }

    private void setLoading(boolean loading) {
        binding.progressLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
        binding.editPassword.setEnabled(!loading);
        binding.editConfirm.setEnabled(!loading);
    }

    private char[] getPassword() {
        String s = binding.editPassword.getText() != null ? binding.editPassword.getText().toString() : "";
        if (s.isEmpty()) {
            Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
            return null;
        }
        return s.toCharArray();
    }

    private char[] getConfirm() {
        String s = binding.editConfirm.getText() != null ? binding.editConfirm.getText().toString() : "";
        return s.isEmpty() ? null : s.toCharArray();
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }

    private void openVault() {
        startActivity(new Intent(this, VaultActivity.class));
        finish();
    }
}
