package com.joycrew.backend.service;

import jakarta.annotation.PreDestroy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationSseService {

    // email을 채널 키로 사용
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private static final long TIMEOUT_MS = Duration.ofMinutes(30).toMillis();

    public SseEmitter subscribe(String userEmail) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emitters.computeIfAbsent(userEmail, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(userEmail, emitter));
        emitter.onTimeout(() -> remove(userEmail, emitter));
        emitter.onError(e -> remove(userEmail, emitter));

        try { emitter.send(SseEmitter.event().name("connected").data("ok").reconnectTime(3000)); }
        catch (IOException ignored) {}

        return emitter;
    }

    public void push(String userEmail, Object payload) {
        List<SseEmitter> list = emitters.getOrDefault(userEmail, new CopyOnWriteArrayList<>());
        for (SseEmitter em : list) {
            try {
                em.send(SseEmitter.event().name("notification").data(payload, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                remove(userEmail, em);
            }
        }
    }

    private void remove(String userEmail, SseEmitter em) {
        List<SseEmitter> list = emitters.get(userEmail);
        if (list != null) list.remove(em);
    }

    @PreDestroy
    public void shutdown() { emitters.clear(); }
}
