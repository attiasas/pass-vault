package com.passvault.app.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Single auth entry (e.g. one service login).
 */
public class AuthEntry {
    private String id;
    private String title;
    private String username;
    private String passwordOrToken; // current value
    private long createdAt;
    private long updatedAt;
    private List<EntryHistoryItem> history;

    public AuthEntry() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.history = new ArrayList<>();
    }

    public AuthEntry(String title, String username, String passwordOrToken) {
        this();
        this.title = title;
        this.username = username;
        this.passwordOrToken = passwordOrToken;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordOrToken() {
        return passwordOrToken;
    }

    public void setPasswordOrToken(String passwordOrToken) {
        this.passwordOrToken = passwordOrToken;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<EntryHistoryItem> getHistory() {
        return history != null ? history : new ArrayList<>();
    }

    public void setHistory(List<EntryHistoryItem> history) {
        this.history = history != null ? history : new ArrayList<>();
    }

    /** Call before updating password: records current value in history. */
    public void pushCurrentToHistory() {
        if (passwordOrToken == null || passwordOrToken.isEmpty()) return;
        long end = System.currentTimeMillis();
        EntryHistoryItem item = new EntryHistoryItem(updatedAt, end, passwordOrToken);
        getHistory().add(item);
    }
}
