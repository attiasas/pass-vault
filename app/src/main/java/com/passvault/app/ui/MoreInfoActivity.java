package com.passvault.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.passvault.app.PassVaultApp;
import com.passvault.app.R;
import com.passvault.app.data.AuthEntry;
import com.passvault.app.data.EntryHistoryItem;
import com.passvault.app.databinding.ActivityMoreInfoBinding;
import com.passvault.app.storage.VaultRepository;
import com.passvault.app.util.HealthCalculator;
import com.passvault.app.util.PasswordStrength;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MoreInfoActivity extends AppCompatActivity {

    public static final String EXTRA_ENTRY_ID = "entry_id";

    private ActivityMoreInfoBinding binding;
    private VaultRepository vault;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMoreInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        vault = ((PassVaultApp) getApplication()).getVaultRepository();
        if (!vault.isUnlocked()) {
            finish();
            return;
        }

        String id = getIntent().getStringExtra(EXTRA_ENTRY_ID);
        AuthEntry entry = null;
        if (id != null) {
            try {
                entry = vault.getEntryWithHistory(id);
            } catch (Exception e) {
                finish();
                return;
            }
        }
        if (entry == null) {
            finish();
            return;
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.title.setText(entry.getTitle() != null ? entry.getTitle() : "");

        int health = HealthCalculator.calculate(entry.getUpdatedAt());
        int daysSince = HealthCalculator.daysSinceUpdate(entry.getUpdatedAt());
        binding.healthValue.setText(String.format(Locale.getDefault(), "%s (%d) — %d days since update",
                HealthCalculator.badgeLabel(health), health, daysSince));

        int strength = PasswordStrength.calculate(entry.getPasswordOrToken() != null ? entry.getPasswordOrToken() : "");
        binding.strengthValue.setText(String.format(Locale.getDefault(), "%s (%d)", PasswordStrength.label(strength), strength));

        binding.startDate.setText(SDF.format(new Date(entry.getCreatedAt())));
        binding.updateDate.setText(SDF.format(new Date(entry.getUpdatedAt())));
        binding.daysUsed.setText(String.format(Locale.getDefault(), "%d days", daysSince));

        List<EntryHistoryItem> history = entry.getHistory();
        HistoryAdapter adapter = new HistoryAdapter(history);
        binding.historyList.setLayoutManager(new LinearLayoutManager(this));
        binding.historyList.setAdapter(adapter);
        binding.historyList.setVisibility(history.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.Holder> {
        private final List<EntryHistoryItem> items;

        HistoryAdapter(List<EntryHistoryItem> items) {
            this.items = items != null ? items : new java.util.ArrayList<>();
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(Holder h, int position) {
            EntryHistoryItem item = items.get(position);
            String start = SDF.format(new Date(item.getStartDate()));
            String end = SDF.format(new Date(item.getEndDate()));
            h.range.setText(start + " → " + end);
            h.days.setText(item.getDaysUsed() + " days");
            String p = item.getPassValue();
            h.passValue.setVisibility(p != null && !p.isEmpty() ? View.VISIBLE : View.GONE);
            h.passValue.setText(p != null ? p : "");
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            TextView range, days, passValue;

            Holder(View itemView) {
                super(itemView);
                range = itemView.findViewById(R.id.range);
                days = itemView.findViewById(R.id.days);
                passValue = itemView.findViewById(R.id.passValue);
            }
        }
    }
}
