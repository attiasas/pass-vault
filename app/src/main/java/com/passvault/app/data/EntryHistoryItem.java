package com.passvault.app.data;

import java.util.Date;

/**
 * One historical record of a password/token that was used for an entry.
 */
public class EntryHistoryItem {
    private long startDate;
    private long endDate;
    private String passValue; // stored encrypted in vault, decrypted when showing

    public EntryHistoryItem() {
    }

    public EntryHistoryItem(long startDate, long endDate, String passValue) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.passValue = passValue;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public String getPassValue() {
        return passValue;
    }

    public void setPassValue(String passValue) {
        this.passValue = passValue;
    }

    public int getDaysUsed() {
        if (endDate <= startDate) return 0;
        return (int) ((endDate - startDate) / (24 * 60 * 60 * 1000L));
    }
}
