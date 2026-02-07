package com.passvault.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.passvault.app.PassVaultApp;
import com.passvault.app.R;
import com.passvault.app.data.EncryptionMethod;
import com.passvault.app.databinding.ActivitySettingsBinding;
import com.passvault.app.storage.StorageType;
import com.passvault.app.storage.VaultRepository;
import com.passvault.app.util.ExportImport;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private VaultRepository vault;
    private ActivityResultLauncher<String> exportLauncher;
    private ActivityResultLauncher<String> importLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        vault = ((PassVaultApp) getApplication()).getVaultRepository();
        if (!vault.isUnlocked()) {
            finish();
            return;
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Storage type (File vs SQL)
        List<String> storageNames = new ArrayList<>();
        for (StorageType t : StorageType.values()) {
            storageNames.add(t.getDisplayName());
        }
        binding.spinnerStorageType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, storageNames));
        int storageIdx = 0;
        for (int i = 0; i < StorageType.values().length; i++) {
            if (StorageType.values()[i] == vault.getStorageType()) {
                storageIdx = i;
                break;
            }
        }
        binding.spinnerStorageType.setSelection(storageIdx);
        binding.spinnerStorageType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                StorageType selected = StorageType.values()[position];
                if (selected == vault.getStorageType()) return;
                try {
                    vault.switchStorageType(selected);
                    Toast.makeText(SettingsActivity.this, getString(R.string.storage_switched), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(SettingsActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Encryption method
        List<String> names = new ArrayList<>();
        for (EncryptionMethod m : EncryptionMethod.values()) {
            names.add(m.getDisplayName());
        }
        binding.spinnerEncryption.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
        int idx = 0;
        for (int i = 0; i < EncryptionMethod.values().length; i++) {
            if (EncryptionMethod.values()[i] == vault.getEncryptionMethod()) {
                idx = i;
                break;
            }
        }
        binding.spinnerEncryption.setSelection(idx);
        binding.spinnerEncryption.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                vault.setEncryptionMethod(EncryptionMethod.values()[position]);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Password reuse check
        binding.editReuseCount.setText(String.valueOf(vault.getReuseCheckCount()));
        binding.editReuseCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String t = s != null ? s.toString().trim() : "";
                    int v = t.isEmpty() ? 3 : Integer.parseInt(t);
                    v = v < 1 ? 1 : (v > 50 ? 50 : v);
                    vault.setReuseCheckCount(v);
                } catch (NumberFormatException ignored) {
                    vault.setReuseCheckCount(3);
                }
            }
        });
        binding.switchEnforceReuse.setChecked(vault.getEnforceReuseCheck());
        binding.switchEnforceReuse.setOnCheckedChangeListener((btn, checked) -> vault.setEnforceReuseCheck(checked));

        // Wrong password: wipe-after attempts and prank-only
        binding.editWipeAfterAttempts.setText(String.valueOf(vault.getWipeAfterAttempts()));
        binding.editWipeAfterAttempts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String t = s != null ? s.toString().trim() : "";
                    int v = t.isEmpty() ? 2 : Integer.parseInt(t);
                    v = v < 1 ? 1 : (v > 10 ? 10 : v);
                    vault.setWipeAfterAttempts(v);
                } catch (NumberFormatException ignored) {
                    vault.setWipeAfterAttempts(2);
                }
            }
        });
        binding.switchPrankOnly.setChecked(vault.getPrankOnly());
        binding.switchPrankOnly.setOnCheckedChangeListener((btn, checked) -> vault.setPrankOnly(checked));

        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        binding.btnExport.setOnClickListener(v -> launchExport());
        binding.btnImport.setOnClickListener(v -> launchImport());
        binding.btnLogout.setOnClickListener(v -> logout());

        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/json"),
                uri -> {
                    if (uri == null) return;
                    try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                        if (out != null) {
                            ExportImport.writeToStream(ExportImport.exportToJson(vault.getAllEntries()), out);
                            Toast.makeText(this, "Exported", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        importLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;
                    try (InputStream in = getContentResolver().openInputStream(uri)) {
                        if (in != null) {
                            String json = ExportImport.readFromStream(in);
                            List<com.passvault.app.data.AuthEntry> imported = ExportImport.importFromJson(json);
                            for (com.passvault.app.data.AuthEntry e : imported) {
                                e.setId(java.util.UUID.randomUUID().toString());
                                e.setHistory(new java.util.ArrayList<>());
                                vault.addEntry(e);
                            }
                            Toast.makeText(this, "Imported " + imported.size() + " entries", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showChangePasswordDialog() {
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        com.google.android.material.textfield.TextInputEditText editOld = view.findViewById(R.id.editOldPassword);
        com.google.android.material.textfield.TextInputEditText editNew = view.findViewById(R.id.editNewPassword);
        com.google.android.material.textfield.TextInputEditText editConfirm = view.findViewById(R.id.editConfirmNew);
        new AlertDialog.Builder(this)
                .setTitle(R.string.change_master_password)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    char[] oldP = editOld.getText() != null ? editOld.getText().toString().toCharArray() : new char[0];
                    char[] newP = editNew.getText() != null ? editNew.getText().toString().toCharArray() : new char[0];
                    char[] conf = editConfirm.getText() != null ? editConfirm.getText().toString().toCharArray() : new char[0];
                    if (newP.length == 0 || !java.util.Arrays.equals(newP, conf)) {
                        Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        vault.changeMasterPassword(oldP, newP);
                        Toast.makeText(this, "Password changed", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void launchExport() {
        exportLauncher.launch("passvault_export.json");
    }

    private void launchImport() {
        importLauncher.launch("application/json");
    }

    private void logout() {
        vault.lock();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
