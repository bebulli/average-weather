package com.rubinukperaj.weather.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WeatherCache<T> {

    private final Duration ttl;
    private final ConcurrentMap<String, Entry<T>> store = new ConcurrentHashMap<>();

    public WeatherCache(Duration ttl) {
        this.ttl = ttl;
    }

    public Optional<T> get(String key) {
        Entry<T> entry = store.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (Instant.now().isAfter(entry.expiresAt())) {
            store.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }

    public void put(String key, T value) {
        store.put(key, new Entry<>(value, Instant.now().plus(ttl)));
    }

    private record Entry<T>(T value, Instant expiresAt) {
    }
}
