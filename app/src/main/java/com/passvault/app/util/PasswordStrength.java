package com.passvault.app.util;

/**
 * Calculates a simple strength value 0-100 for a password.
 */
public final class PasswordStrength {

    private PasswordStrength() {
    }

    public static int calculate(String password) {
        if (password == null || password.isEmpty()) return 0;
        int score = 0;
        int len = password.length();
        if (len >= 8) score += 15;
        if (len >= 12) score += 10;
        if (len >= 16) score += 10;
        boolean hasLower = false, hasUpper = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        if (hasLower) score += 15;
        if (hasUpper) score += 15;
        if (hasDigit) score += 15;
        if (hasSpecial) score += 20;
        return Math.min(100, score);
    }

    public static String label(int strength) {
        if (strength < 25) return "Weak";
        if (strength < 50) return "Fair";
        if (strength < 75) return "Good";
        return "Strong";
    }
}
