package dev.tvedeane;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Redis {
    private final ConcurrentHashMap<String, Entry> storage = new ConcurrentHashMap<>();

    public Entry getEntry(String key) {
        return storage.get(key);
    }

    public void addSingle(String key, String value) {
        storage.put(key, Entry.ofSingle(value));
    }

    public void addMultiple(String key, String value) {
        storage.compute(key, (k, v) -> {
            if (v == null) {
                return Entry.ofMultiple(value);
            }
            v.addMultiple(value);
            return v;
        });
    }

    public int removeMultiple(String key, String value, int count) {
        if (count == 0) {
            var removedCount = new AtomicInteger(0);
            storage.computeIfPresent(key, (k, v) -> {
                var originalSize = v.getMultipleSize();
                v.removeItemsMatching(value);
                removedCount.set(originalSize - v.getMultipleSize());
                return v;
            });
            return removedCount.get();
        }
        return -1;
    }
}
