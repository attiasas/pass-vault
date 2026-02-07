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
    /** Consecutive wrong-password attempts (prank: fake “delete” warnings). Reset on success. */
    private int failedAttempts = 0;

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
                    failedAttempts++;
                    showPrankMessage();
                    return;
                }
                failedAttempts = 0;
                hidePrankMessage();
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
                final Exception resultError = error;
                runOnUiThread(() -> {
                    setLoading(false);
                    if (resultError != null) {
                        Toast.makeText(this, "Error: " + resultError.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        failedAttempts = 0;
                        hidePrankMessage();
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

    /** Prank: show escalating fake “delete” / mock messages. No real data is ever deleted. */
    private void showPrankMessage() {
        String message;
        if (failedAttempts == 1) {
            message = getString(R.string.prank_warn_next);
        } else if (failedAttempts == 2) {
            message = getString(R.string.prank_deleted_mock);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } else {
            String[] options = getResources().getStringArray(R.array.prank_meaningless);
            message = options[new java.util.Random().nextInt(options.length)];
        }
        binding.prankMessageText.setText(message);
        binding.prankMessageCard.setVisibility(View.VISIBLE);
    }

    private void hidePrankMessage() {
        binding.prankMessageCard.setVisibility(View.GONE);
    }
}
