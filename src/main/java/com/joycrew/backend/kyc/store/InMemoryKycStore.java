package com.joycrew.backend.kyc.store;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryKycStore {
    @Getter
    @AllArgsConstructor
    public static class Entry {
        private final String tokenVal;
        private final Instant createdAt;
    }

    private final Map<String, Entry> map = new ConcurrentHashMap<>();
    private final long ttlSeconds;

    public InMemoryKycStore(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public void put(String reqNo, String tokenVal) {
        Objects.requireNonNull(reqNo);
        Objects.requireNonNull(tokenVal);
        map.put(reqNo, new Entry(tokenVal, Instant.now()));
    }

    public String takeIfFresh(String reqNo) {
        Entry e = map.remove(reqNo);
        if (e == null) return null;
        if (Instant.now().isAfter(e.getCreatedAt().plusSeconds(ttlSeconds))) return null;
        return e.getTokenVal();
    }

    // 청소용 (선택)
    public void purgeExpired() {
        Instant now = Instant.now();
        map.entrySet().removeIf(en -> now.isAfter(en.getValue().getCreatedAt().plusSeconds(ttlSeconds)));
    }
}
