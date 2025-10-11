package com.example.util;

public final class EnvUtil {

    private EnvUtil() {
    }

    public static int parsePositiveInt(String raw, int defaultVal, int min, int max) {
        if (raw == null || raw.isBlank()) return defaultVal;
        try {
            int val = Integer.parseInt(raw.trim());
            if (val < min) return min;
            if (val > max) return max;
            return val;
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    public static String defaultIfBlank(String value, String def) {
        return (value == null || value.isBlank()) ? def : value.trim();
    }
}
