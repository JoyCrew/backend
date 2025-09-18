package com.joycrew.backend.util;

public final class MaskingUtil {
    private MaskingUtil() {}

    public static String maskEmail(String email) {
        if (email == null) return null;
        int at = email.indexOf('@');
        if (at <= 0) return email;
        String id = email.substring(0, at);
        if (id.length() <= 2) return id.charAt(0) + "*" + email.substring(at);
        return id.substring(0, 2) + "***" + email.substring(at - 1, at) + email.substring(at);
    }
}
