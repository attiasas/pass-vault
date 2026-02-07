package com.passvault.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.passvault.app.PassVaultApp;
import com.passvault.app.R;
import com.passvault.app.data.AuthEntry;
import com.passvault.app.databinding.ActivityVaultBinding;
import com.passvault.app.storage.VaultRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VaultActivity extends AppCompatActivity implements EntriesAdapter.Listener {

    private ActivityVaultBinding binding;
    private VaultRepository vault;
    private EntriesAdapter adapter;
    private List<AuthEntry> allEntries = new ArrayList<>();
    private String searchQuery = "";
    private static final int[] HEALTH_COLORS = new int[]{
            0xFF10B981, 0xFFF59E0B, 0xFFEF4444
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVaultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        vault = ((PassVaultApp) getApplication()).getVaultRepository();
        if (!vault.isUnlocked()) {
            finish();
            return;
        }
        setSupportActionBar(binding.toolbar);
        if (binding.toolbar.getOverflowIcon() != null) {
            DrawableCompat.setTint(binding.toolbar.getOverflowIcon(), ContextCompat.getColor(this, R.color.white));
        }

        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_generator) {
                startActivity(new Intent(this, PasswordGeneratorActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.menu_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });

        adapter = new EntriesAdapter(this, HEALTH_COLORS);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);
        refreshList();

        binding.searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s != null ? s.toString().trim().toLowerCase(Locale.getDefault()) : "";
                applyFilter();
            }
        });

        binding.fab.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, AddEditEntryActivity.class), 1);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_vault, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (vault.isUnlocked()) refreshList();
    }

    private void refreshList() {
        allEntries = vault.getAllEntries();
        if (allEntries == null) allEntries = new ArrayList<>();
        applyFilter();
    }

    private void applyFilter() {
        List<AuthEntry> filtered = allEntries;
        if (searchQuery != null && !searchQuery.isEmpty()) {
            filtered = new ArrayList<>();
            for (AuthEntry e : allEntries) {
                String title = e.getTitle();
                if (title != null && title.toLowerCase(Locale.getDefault()).contains(searchQuery)) {
                    filtered.add(e);
                }
            }
        }
        adapter.setEntries(filtered);
        int total = allEntries.size();
        int shown = filtered.size();
        if (searchQuery == null || searchQuery.isEmpty()) {
            binding.entryCount.setText(getString(R.string.entries_count, total));
        } else {
            binding.entryCount.setText(getString(R.string.entries_count_filtered, shown, total));
        }
    }

    @Override
    public void onEditEntryClick(AuthEntry entry) {
        Intent i = new Intent(this, AddEditEntryActivity.class);
        i.putExtra(AddEditEntryActivity.EXTRA_ENTRY_ID, entry.getId());
        startActivityForResult(i, 1);
    }

    @Override
    public void onMoreInfoClick(AuthEntry entry) {
        Intent i = new Intent(this, MoreInfoActivity.class);
        i.putExtra(MoreInfoActivity.EXTRA_ENTRY_ID, entry.getId());
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) refreshList();
    }
}
