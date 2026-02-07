package com.passvault.app.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.passvault.app.PassVaultApp;
import com.passvault.app.R;
import com.passvault.app.data.AuthEntry;
import com.passvault.app.data.EntryHistoryItem;
import com.passvault.app.databinding.ActivityAddEditEntryBinding;
import com.passvault.app.storage.VaultRepository;
import com.passvault.app.util.PasswordStrength;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AddEditEntryActivity extends AppCompatActivity {

    public static final String EXTRA_ENTRY_ID = "entry_id";

    private ActivityAddEditEntryBinding binding;
    private VaultRepository vault;
    private AuthEntry entry;
    private boolean isEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        vault = ((PassVaultApp) getApplication()).getVaultRepository();
        if (!vault.isUnlocked()) {
            finish();
            return;
        }

        String id = getIntent().getStringExtra(EXTRA_ENTRY_ID);
        isEdit = id != null;
        if (isEdit) {
            try {
                entry = vault.getEntryWithHistory(id);
            } catch (Exception e) {
                finish();
                return;
            }
            if (entry == null) {
                finish();
                return;
            }
            binding.toolbar.setTitle(R.string.edit_entry);
            binding.editTitle.setText(entry.getTitle());
            binding.editUsername.setText(entry.getUsername());
            binding.editPassword.setText(entry.getPasswordOrToken());
            binding.btnDelete.setVisibility(android.view.View.VISIBLE);
        } else {
            entry = new AuthEntry();
            binding.btnDelete.setVisibility(android.view.View.GONE);
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> save());
        binding.btnDelete.setOnClickListener(v -> confirmDelete());
        binding.btnFromGenerator.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, PasswordGeneratorActivity.class)
                    .putExtra(PasswordGeneratorActivity.EXTRA_RETURN_PASSWORD, true), 2);
        });
        binding.editPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateStrengthDisplay();
                updateReuseDisplay();
            }
        });
        updateStrengthDisplay();
        updateReuseDisplay();
    }

    private void save() {
        String title = binding.editTitle.getText() != null ? binding.editTitle.getText().toString().trim() : "";
        String username = binding.editUsername.getText() != null ? binding.editUsername.getText().toString().trim() : "";
        String password = binding.editPassword.getText() != null ? binding.editPassword.getText().toString() : "";
        if (title.isEmpty()) {
            Toast.makeText(this, "Title required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, getString(R.string.password_required), Toast.LENGTH_SHORT).show();
            return;
        }
        if (isEdit && vault.getEnforceReuseCheck() && isPasswordReused(entry, password, vault.getReuseCheckCount())) {
            Toast.makeText(this, getString(R.string.reuse_blocked, vault.getReuseCheckCount()), Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEdit) {
            String oldPass = entry.getPasswordOrToken();
            if (oldPass != null && !oldPass.equals(password)) {
                entry.pushCurrentToHistory();
            }
            entry.setTitle(title);
            entry.setUsername(username);
            entry.setPasswordOrToken(password);
            entry.setUpdatedAt(System.currentTimeMillis());
            vault.updateEntry(entry);
        } else {
            entry.setTitle(title);
            entry.setUsername(username);
            entry.setPasswordOrToken(password);
            vault.addEntry(entry);
        }
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage("Delete this entry?")
                .setPositiveButton(android.R.string.yes, (d, w) -> {
                    vault.deleteEntry(entry.getId());
                    setResult(RESULT_OK);
                    finish();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            String pass = data.getStringExtra(PasswordGeneratorActivity.EXTRA_PASSWORD_RESULT);
            if (pass != null) {
                binding.editPassword.setText(pass);
                updateStrengthDisplay();
            }
        }
    }

    private void updateStrengthDisplay() {
        String pass = binding.editPassword.getText() != null ? binding.editPassword.getText().toString() : "";
        int strength = PasswordStrength.calculate(pass);
        binding.strengthProgress.setProgress(strength);
        binding.strengthProgress.setProgressTintList(ColorStateList.valueOf(strengthColor(strength)));
        binding.strengthValue.setText(String.format(Locale.getDefault(), "%s (%d)", PasswordStrength.label(strength), strength));
    }

    /** True if {@code newPassword} appears in the last X passwords (current + recent history). */
    private static boolean isPasswordReused(AuthEntry entry, String newPassword, int X) {
        if (newPassword == null || X < 1) return false;
        Set<String> lastX = new HashSet<>();
        String current = entry.getPasswordOrToken();
        if (current != null && !current.isEmpty()) lastX.add(current);
        List<EntryHistoryItem> hist = entry.getHistory();
        for (int i = hist.size() - 1; i >= 0 && lastX.size() < X; i--) {
            String p = hist.get(i).getPassValue();
            if (p != null && !p.isEmpty()) lastX.add(p);
        }
        return lastX.contains(newPassword);
    }

    private void updateReuseDisplay() {
        if (!isEdit) {
            binding.reuseMessage.setVisibility(android.view.View.GONE);
            binding.btnSave.setEnabled(true);
            return;
        }
        String password = binding.editPassword.getText() != null ? binding.editPassword.getText().toString() : "";
        int X = vault.getReuseCheckCount();
        boolean reused = isPasswordReused(entry, password, X);
        binding.reuseMessage.setVisibility(android.view.View.VISIBLE);
        if (reused) {
            binding.reuseMessage.setText(getString(R.string.reuse_warning, X));
            binding.reuseMessage.setTextColor(ContextCompat.getColor(this, R.color.health_bad));
            binding.btnSave.setEnabled(!vault.getEnforceReuseCheck());
        } else {
            binding.reuseMessage.setText(getString(R.string.reuse_ok, X));
            binding.reuseMessage.setTextColor(ContextCompat.getColor(this, R.color.health_good));
            binding.btnSave.setEnabled(true);
        }
    }

    private int strengthColor(int strength) {
        if (strength >= 75) return ContextCompat.getColor(this, R.color.health_good);
        if (strength >= 50) return ContextCompat.getColor(this, R.color.health_warning);
        return ContextCompat.getColor(this, R.color.health_bad);
    }
}
