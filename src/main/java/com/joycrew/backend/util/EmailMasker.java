package com.joycrew.backend.util;

public class EmailMasker {
    public static String mask(String email) {
        if (email == null || email.isBlank()) return null;
        int at = email.indexOf('@');
        if (at <= 1) return "*".repeat(Math.max(1, at)) + email.substring(Math.max(at, 0));
        String name = email.substring(0, at);
        String domain = email.substring(at);
        String masked = name.charAt(0) + "***" + name.charAt(name.length()-1);
        return masked + domain;
    }
}