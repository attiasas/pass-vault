package com.passvault.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.passvault.app.R;
import com.passvault.app.data.AuthEntry;
import com.passvault.app.util.HealthCalculator;

import java.util.ArrayList;
import java.util.List;

public class EntriesAdapter extends RecyclerView.Adapter<EntriesAdapter.Holder> {

    public interface Listener {
        void onEntryClick(AuthEntry entry);
        void onMoreInfoClick(AuthEntry entry);
    }

    private final List<AuthEntry> entries = new ArrayList<>();
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

        boolean revealed = h.revealed;
        String pass = e.getPasswordOrToken();
        h.passHidden.setText(revealed && pass != null ? pass : "••••••••");
        h.btnReveal.setText(revealed ? R.string.hide : R.string.show);

        h.btnReveal.setOnClickListener(v -> {
            h.revealed = !h.revealed;
            notifyItemChanged(position);
        });

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEntryClick(e);
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

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView title, username, passHidden, healthBadge;
        MaterialButton btnReveal;
        View btnMore;
        boolean revealed;

        Holder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            username = itemView.findViewById(R.id.username);
            passHidden = itemView.findViewById(R.id.passHidden);
            healthBadge = itemView.findViewById(R.id.healthBadge);
            btnReveal = itemView.findViewById(R.id.btnReveal);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
