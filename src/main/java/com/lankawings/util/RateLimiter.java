package com.lankawings.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Small in-memory limiter used against brute force (login, password checks, card attempts, sign-up spam).
 * record(key) counts an event; after "max" events inside "windowMs" the key is locked for "lockMs".
 * (In-memory means it resets when Tomcat restarts and is per server instance - fine for this project.)
 */
public final class RateLimiter {
    private static final class Bucket { int count; long windowStart; long lockedUntil; }

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final int max;
    private final long windowMs;
    private final long lockMs;

    public RateLimiter(int max, long windowMs, long lockMs) { this.max = max; this.windowMs = windowMs; this.lockMs = lockMs; }

    public boolean isBlocked(String key) {
        Bucket b = buckets.get(key);
        return b != null && b.lockedUntil > System.currentTimeMillis();
    }

    public long secondsLeft(String key) {
        Bucket b = buckets.get(key);
        if (b == null) return 0;
        return Math.max(0, (b.lockedUntil - System.currentTimeMillis() + 999) / 1000);
    }

    public void record(String key) {
        long now = System.currentTimeMillis();
        if (buckets.size() > 20_000) buckets.entrySet().removeIf(e -> e.getValue().lockedUntil < now && now - e.getValue().windowStart > windowMs);
        Bucket b = buckets.computeIfAbsent(key, k -> new Bucket());
        synchronized (b) {
            if (now - b.windowStart > windowMs) { b.windowStart = now; b.count = 0; }
            b.count++;
            if (b.count >= max) { b.lockedUntil = now + lockMs; b.count = 0; b.windowStart = now; }
        }
    }

    public void reset(String key) { buckets.remove(key); }

    public String waitMessage(String key) {
        long s = secondsLeft(key);
        long m = (s + 59) / 60;
        return "Too many attempts. Please try again in " + (m <= 1 ? "1 minute" : m + " minutes") + ".";
    }
}
