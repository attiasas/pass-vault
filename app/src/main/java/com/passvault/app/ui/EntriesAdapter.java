package com.passvault.app.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.passvault.app.R;
import com.passvault.app.data.AuthEntry;
import com.passvault.app.util.HealthCalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntriesAdapter extends RecyclerView.Adapter<EntriesAdapter.Holder> {

    public interface Listener {
        void onEditEntryClick(AuthEntry entry);
        void onMoreInfoClick(AuthEntry entry);
    }

    private final List<AuthEntry> entries = new ArrayList<>();
    private final Map<String, Boolean> revealedByEntryId = new HashMap<>();
    private final Listener listener;
    private final int[] healthColors;

    public EntriesAdapter(Listener listener, int[] healthColors) {
        this.listener = listener;
        this.healthColors = healthColors;
    }

    public void setEntries(List<AuthEntry> list) {
        entries.clear();
        if (list != null) entries.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entry, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        AuthEntry e = entries.get(position);
        h.title.setText(e.getTitle() != null ? e.getTitle() : "");
        h.username.setText(e.getUsername() != null ? e.getUsername() : "");

        int health = HealthCalculator.calculate(e.getUpdatedAt());
        h.healthBadge.setText(HealthCalculator.badgeLabel(health));
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        bg.setCornerRadius(4f * h.healthBadge.getResources().getDisplayMetrics().density);
        bg.setColor(healthColors[healthIndex(health)]);
        h.healthBadge.setBackground(bg);
        h.healthBadge.setTextColor(android.graphics.Color.WHITE);

        boolean revealed = Boolean.TRUE.equals(revealedByEntryId.get(e.getId()));
        String pass = e.getPasswordOrToken();
        h.passHidden.setText(revealed && pass != null ? pass : "••••••••");
        h.btnCopyPass.setVisibility(revealed && pass != null && !pass.isEmpty() ? View.VISIBLE : View.GONE);
        h.btnCopyPass.setOnClickListener(v -> {
            if (pass != null && !pass.isEmpty()) {
                ClipboardManager cm = (ClipboardManager) h.itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null) {
                    cm.setPrimaryClip(ClipData.newPlainText("password", pass));
                    Toast.makeText(h.itemView.getContext(), R.string.copied, Toast.LENGTH_SHORT).show();
                }
            }
        });

        h.itemView.setOnClickListener(v -> {
            if (revealed) {
                revealedByEntryId.put(e.getId(), false);
                notifyItemChanged(position);
            } else {
                new AlertDialog.Builder(h.itemView.getContext())
                        .setMessage(R.string.confirm_display_password)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            revealedByEntryId.put(e.getId(), true);
                            int pos = findPositionByEntryId(e.getId());
                            if (pos >= 0) notifyItemChanged(pos);
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });

        h.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditEntryClick(e);
        });
        h.btnMore.setOnClickListener(v -> {
            if (listener != null) listener.onMoreInfoClick(e);
        });
    }

    private int healthIndex(int health) {
        if (health >= 80) return 0;
        if (health >= 50) return 1;
        return 2;
    }

    private int findPositionByEntryId(String id) {
        for (int i = 0; i < entries.size(); i++) {
            if (id.equals(entries.get(i).getId())) return i;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView title, username, passHidden, healthBadge;
        View btnEdit, btnMore, btnCopyPass;

        Holder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            username = itemView.findViewById(R.id.username);
            passHidden = itemView.findViewById(R.id.passHidden);
            healthBadge = itemView.findViewById(R.id.healthBadge);
            btnCopyPass = itemView.findViewById(R.id.btnCopyPass);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
