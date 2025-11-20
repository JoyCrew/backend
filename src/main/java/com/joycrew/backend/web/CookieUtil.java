// com.joycrew.backend.web.CookieUtil.java
package com.joycrew.backend.web;

import org.springframework.http.ResponseCookie;

public class CookieUtil {
    public static ResponseCookie authCookie(String token, String domain, long maxAgeSeconds, boolean secure) {
        return ResponseCookie.from("JC_AUTH", token)
                .httpOnly(true)
                .secure(secure)                 // prod: true, dev: false 가능
                .sameSite("None")               // cross-site 이동을 위해 None
                .domain(domain)                 // ".joycrew.co.kr"
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    public static ResponseCookie clearAuth(String domain, boolean secure) {
        return ResponseCookie.from("JC_AUTH", "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("None")
                .domain(domain)
                .path("/")
                .maxAge(0)
                .build();
    }
}
