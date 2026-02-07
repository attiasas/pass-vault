package com.passvault.app.ui;

import android.content.Intent;
import android.os.Bundle;
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

public class VaultActivity extends AppCompatActivity implements EntriesAdapter.Listener {

    private ActivityVaultBinding binding;
    private VaultRepository vault;
    private EntriesAdapter adapter;
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
        adapter.setEntries(vault.getAllEntries());
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
