package com.passvault.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.passvault.app.PassVaultApp;
import com.passvault.app.R;
import com.passvault.app.databinding.ActivityLoginBinding;
import com.passvault.app.storage.VaultRepository;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private VaultRepository vault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        vault = ((PassVaultApp) getApplication()).getVaultRepository();

        boolean isNew = !vault.isVaultCreated();
        binding.layoutConfirm.setVisibility(isNew ? android.view.View.VISIBLE : android.view.View.GONE);
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
                try {
                    vault.createVault(pass);
                    openVault();
                } catch (Exception e) {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                if (!vault.verifyPassword(pass)) {
                    Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    vault.unlock(pass);
                    openVault();
                } catch (Exception e) {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    private void openVault() {
        startActivity(new Intent(this, VaultActivity.class));
        finish();
    }
}
