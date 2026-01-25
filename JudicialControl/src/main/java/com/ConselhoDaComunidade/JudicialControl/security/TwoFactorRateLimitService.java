package com.ConselhoDaComunidade.JudicialControl.security;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class TwoFactorRateLimitService {

    private static class Entry {
        int attempts;
        long blockedUntil;
        long windowStart;
    }

    private final ConcurrentHashMap<String, Entry> map = new ConcurrentHashMap<>();

    // Exemplo: 6 tentativas em 2 minutos, bloqueia 10 minutos
    private static final int MAX_ATTEMPTS = 6;
    private static final long WINDOW_MS = 2 * 60_000L;
    private static final long BLOCK_MS = 10 * 60_000L;

    public boolean isBlocked(String key) {
        Entry e = map.get(key);
        return e != null && e.blockedUntil > System.currentTimeMillis();
    }

    public void onFailure(String key) {
        long now = System.currentTimeMillis();
        Entry e = map.computeIfAbsent(key, k -> {
            Entry n = new Entry();
            n.windowStart = now;
            return n;
        });

        synchronized (e) {
            if (now - e.windowStart > WINDOW_MS) {
                e.windowStart = now;
                e.attempts = 0;
            }
            e.attempts++;

            if (e.attempts >= MAX_ATTEMPTS) {
                e.blockedUntil = now + BLOCK_MS;
                e.attempts = 0;
                e.windowStart = now;
            }
        }
    }

    public void onSuccess(String key) {
        map.remove(key);
    }
}
