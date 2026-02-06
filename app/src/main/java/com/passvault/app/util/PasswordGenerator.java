package com.passvault.app.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates random passwords with configurable options.
 */
public final class PasswordGenerator {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{}|;:',.<>?";

    private final SecureRandom random = new SecureRandom();

    public String generate(int length, boolean useDigits, boolean useUpper, boolean useSpecial, float lowerCaseRatio) {
        if (length <= 0) return "";
        lowerCaseRatio = Math.max(0f, Math.min(1f, lowerCaseRatio));

        List<Character> chars = new ArrayList<>();
        chars.add(LOWER.charAt(random.nextInt(LOWER.length())));
        if (useUpper) chars.add(UPPER.charAt(random.nextInt(UPPER.length())));
        if (useDigits) chars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        if (useSpecial) chars.add(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        StringBuilder other = new StringBuilder();
        if (useUpper) other.append(UPPER);
        if (useDigits) other.append(DIGITS);
        if (useSpecial) other.append(SPECIAL);
        if (other.length() == 0) other.append(LOWER);

        int remaining = length - chars.size();
        for (int i = 0; i < remaining; i++) {
            if (random.nextFloat() < lowerCaseRatio) {
                chars.add(LOWER.charAt(random.nextInt(LOWER.length())));
            } else {
                chars.add(other.charAt(random.nextInt(other.length())));
            }
        }

        Collections.shuffle(chars, random);
        StringBuilder sb = new StringBuilder(chars.size());
        for (Character c : chars) sb.append(c);
        return sb.toString();
    }
}
