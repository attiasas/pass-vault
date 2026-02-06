package com.passvault.app.util;

/**
 * Health score 0-100 based on days since last update.
 * Older = lower health.
 */
public final class HealthCalculator {

    private static final int DAY_MS = 24 * 60 * 60 * 1000;

    private HealthCalculator() {
    }

    public static int calculate(long updatedAtMillis) {
        long now = System.currentTimeMillis();
        long daysSinceUpdate = (now - updatedAtMillis) / DAY_MS;
        if (daysSinceUpdate <= 30) return 100;
        if (daysSinceUpdate <= 90) return 80;
        if (daysSinceUpdate <= 180) return 60;
        if (daysSinceUpdate <= 365) return 40;
        return Math.max(0, 20 - (int) (daysSinceUpdate - 365) / 30);
    }

    public static int daysSinceUpdate(long updatedAtMillis) {
        return (int) ((System.currentTimeMillis() - updatedAtMillis) / DAY_MS);
    }

    public static String badgeLabel(int health) {
        if (health >= 80) return "Good";
        if (health >= 50) return "Fair";
        if (health >= 25) return "Low";
        return "Stale";
    }
}
