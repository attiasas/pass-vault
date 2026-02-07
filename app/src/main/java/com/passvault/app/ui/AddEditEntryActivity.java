package com.passvault.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.passvault.app.PassVaultApp;
import com.passvault.app.R;
import com.passvault.app.data.AuthEntry;
import com.passvault.app.databinding.ActivityAddEditEntryBinding;
import com.passvault.app.storage.VaultRepository;

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
    }

    private void save() {
        String title = binding.editTitle.getText() != null ? binding.editTitle.getText().toString().trim() : "";
        String username = binding.editUsername.getText() != null ? binding.editUsername.getText().toString().trim() : "";
        String password = binding.editPassword.getText() != null ? binding.editPassword.getText().toString() : "";
        if (title.isEmpty()) {
            Toast.makeText(this, "Title required", Toast.LENGTH_SHORT).show();
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
            if (pass != null) binding.editPassword.setText(pass);
        }
    }
}
